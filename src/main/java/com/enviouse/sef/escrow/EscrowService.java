package com.enviouse.sef.escrow;

import com.enviouse.sef.control.CommunityCommands;
import com.enviouse.sef.control.ServerControlExecutionService;
import com.enviouse.sef.control.ServerControlRepository;
import com.enviouse.sef.economy.EconomyMoney;
import com.enviouse.sef.economy.EconomyProvider;
import com.enviouse.sef.kernel.ActionResult;
import com.enviouse.sef.kernel.KernelServices;
import com.enviouse.sef.recovery.ItemStackSnapshotCodec;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public final class EscrowService {
    private static final UUID ESCROW_ACCOUNT_ID =
            UUID.nameUUIDFromBytes("sef:escrow".getBytes(java.nio.charset.StandardCharsets.UTF_8));
    private static final String RESERVATION_MARKER_KEY = "sef_escrow_reservation";
    private static final String DELIVERY_MARKER_KEY = "sef_escrow_delivery";
    private static final long DEFAULT_EXPIRY_SECONDS = 604_800L;
    private static final int MAXIMUM_SELECTIONS = 64;

    private final EscrowRepository repository;

    public EscrowService(EscrowRepository repository) {
        this.repository = Objects.requireNonNull(repository, "repository");
    }

    public ActionResult<String> execute(
            ServerControlRepository.ControlRecord control,
            ServerControlExecutionService.ExecutionContext context
    ) {
        if (!(context.server() instanceof MinecraftServer server)
                || !(context.source() instanceof CommandSourceStack source)) {
            return ActionResult.failure(
                    ActionResult.ReasonCode.PROVIDER_ERROR,
                    "escrow requires a logical server command context");
        }
        String operation = field(control, "operation", "create").toLowerCase(Locale.ROOT);
        ServerPlayer actor = source.getPlayer();
        if (actor == null) {
            try {
                if ("freeze".equals(operation) || "suspend".equals(operation)) {
                    return freeze(control, control.ownerId());
                }
                if ("unfreeze".equals(operation)
                        || "release".equals(operation)
                        || "reopen".equals(operation)) {
                    return unfreeze(control, control.ownerId());
                }
            } catch (IllegalArgumentException exception) {
                return ActionResult.failure(
                        ActionResult.ReasonCode.INVALID_INPUT,
                        Objects.requireNonNullElse(exception.getMessage(), "escrow input is invalid"));
            }
            return ActionResult.failure(
                    ActionResult.ReasonCode.INVALID_INPUT,
                    "escrow value operations require an online player");
        }
        try {
            return switch (operation) {
                case "create", "send", "publish" -> create(control, actor);
                case "accept", "claim", "buy", "settle" -> claim(control, actor);
                case "bid" -> bid(control, actor);
                case "watch" -> watch(control, actor.getUUID(), true);
                case "unwatch" -> watch(control, actor.getUUID(), false);
                case "decline", "return", "cancel" -> returnValue(control, actor);
                case "freeze", "suspend" -> freeze(control, actor.getUUID());
                case "unfreeze", "release", "reopen" -> unfreeze(control, actor.getUUID());
                case "recover" -> {
                    ActionResult<RecoverySummary> result = reconcilePlayer(actor);
                    yield result.successful()
                            ? ActionResult.success(
                            "escrow recovery completed, " + result.value().completed()
                                    + " completed, " + result.value().released() + " released")
                            : ActionResult.failure(result.reason(), result.detail());
                }
                default -> ActionResult.failure(
                        ActionResult.ReasonCode.INVALID_INPUT,
                        "escrow operation is invalid");
            };
        } catch (IllegalArgumentException | ArithmeticException exception) {
            return ActionResult.failure(
                    ActionResult.ReasonCode.INVALID_INPUT,
                    Objects.requireNonNullElse(exception.getMessage(), "escrow input is invalid"));
        } catch (IllegalStateException exception) {
            return ActionResult.failure(
                    ActionResult.ReasonCode.PROVIDER_ERROR,
                    Objects.requireNonNullElse(exception.getMessage(), "escrow provider is unavailable"));
        }
    }

    public synchronized ActionResult<RecoverySummary> reconcilePlayer(ServerPlayer player) {
        Objects.requireNonNull(player, "player");
        int completed = 0;
        int released = 0;

        ActionResult<Boolean> reservation = reconcileReservationMarker(player);
        if (!reservation.successful()) {
            return ActionResult.failure(reservation.reason(), reservation.detail());
        }
        if (reservation.value()) {
            completed++;
        }

        ActionResult<Boolean> delivery = reconcileDeliveryMarker(player);
        if (!delivery.successful()) {
            return ActionResult.failure(delivery.reason(), delivery.detail());
        }
        if (delivery.value()) {
            completed++;
        }

        CompoundTag data = player.getPersistentData();
        boolean hasReservationMarker = !data.getString(RESERVATION_MARKER_KEY).isBlank();
        boolean hasDeliveryMarker = !data.getString(DELIVERY_MARKER_KEY).isBlank();
        for (EscrowRepository.EscrowRecord record : repository.recoveryRecords(player.getUUID())) {
            if (record.state() == EscrowRepository.EscrowState.PREPARING
                    && record.ownerId().equals(player.getUUID())
                    && !hasReservationMarker) {
                ActionResult<EscrowRepository.EscrowRecord> result = repository.replaceAndFlush(
                        record.transition(
                                EscrowRepository.EscrowState.RETURNED,
                                player.getUUID(),
                                "reservation was not committed to player data",
                                Instant.now()),
                        record.revision(),
                        Set.of(EscrowRepository.EscrowState.PREPARING));
                if (!result.successful()) {
                    return ActionResult.failure(result.reason(), result.detail());
                }
                released++;
            } else if ((record.state() == EscrowRepository.EscrowState.SETTLING
                    && player.getUUID().equals(record.settlementActorId())
                    || record.state() == EscrowRepository.EscrowState.RETURNING
                    && player.getUUID().equals(record.ownerId()))
                    && !hasDeliveryMarker) {
                EscrowRepository.EscrowState fallback = record.state()
                        == EscrowRepository.EscrowState.RETURNING
                        ? EscrowRepository.EscrowState.HELD
                        : EscrowRepository.EscrowState.HELD;
                ActionResult<EscrowRepository.EscrowRecord> result = repository.replaceAndFlush(
                        record.transition(
                                fallback,
                                player.getUUID(),
                                "delivery was not committed to player data",
                                Instant.now()),
                        record.revision(),
                        Set.of(record.state()));
                if (!result.successful()) {
                    return ActionResult.failure(result.reason(), result.detail());
                }
                released++;
            } else if (record.state() == EscrowRepository.EscrowState.BIDDING
                    && player.getUUID().equals(record.pendingBidderId())) {
                ActionResult<EscrowRepository.EscrowRecord> result = completeBid(record);
                if (!result.successful()) {
                    return ActionResult.failure(result.reason(), result.detail());
                }
                completed++;
            }
        }

        for (EscrowRepository.EscrowRecord record : repository.recordsFor(player.getUUID())) {
            if (record.state() == EscrowRepository.EscrowState.HELD
                    && !record.expiresAt().isAfter(Instant.now())
                    && record.ownerId().equals(player.getUUID())
                    && !(record.domain() == EscrowRepository.EscrowDomain.AUCTION
                    && record.highestBidderId() != null)) {
                ActionResult<String> result = returnHeld(record, player);
                if (!result.successful()) {
                    return ActionResult.failure(result.reason(), result.detail());
                }
                completed++;
            }
        }
        return ActionResult.success(new RecoverySummary(completed, released));
    }

    public EscrowRepository repository() {
        return repository;
    }

    private synchronized ActionResult<String> create(
            ServerControlRepository.ControlRecord control,
            ServerPlayer owner
    ) {
        EscrowRepository.EscrowRecord existing = repository.find(control.id()).orElse(null);
        if (existing != null) {
            if (!existing.ownerId().equals(owner.getUUID())
                    || existing.domain() != EscrowRepository.EscrowDomain.fromFeature(control.featureId())) {
                return ActionResult.failure(
                        ActionResult.ReasonCode.CONFLICT,
                        "escrow id was reused for another transaction");
            }
            return resumeReservation(existing, owner);
        }

        EscrowRepository.EscrowDomain domain =
                EscrowRepository.EscrowDomain.fromFeature(control.featureId());
        UUID beneficiary = beneficiary(control, domain, owner.getUUID());
        if ((domain == EscrowRepository.EscrowDomain.PARCEL
                || domain == EscrowRepository.EscrowDomain.TRADE)
                && blockedBy(beneficiary, owner.getUUID(), domain == EscrowRepository.EscrowDomain.PARCEL
                ? "parcels"
                : "trade")) {
            return ActionResult.failure(
                    ActionResult.ReasonCode.TARGET_DENIED,
                    "the recipient blocks this interaction");
        }
        String selectionInput = switch (domain) {
            case PARCEL, LOST_FOUND -> field(control, "items", "");
            case TRADE -> field(control, "offer", "");
            case AUCTION -> field(control, "item", "");
        };
        List<Selection> selections = parseSelections(owner, selectionInput);
        List<ItemStack> selectedItems = captureSelections(owner, selections);
        List<ItemStackSnapshotCodec.SlotStack> items =
                ItemStackSnapshotCodec.captureStacks(selectedItems, owner.registryAccess());

        EconomyProvider provider = null;
        long reservedCurrency = 0L;
        long price = 0L;
        String currencyInput = domain == EscrowRepository.EscrowDomain.TRADE
                ? field(control, "currency", "")
                : domain == EscrowRepository.EscrowDomain.PARCEL
                ? field(control, "currency", "")
                : "";
        if (!currencyInput.isBlank()) {
            provider = KernelServices.economy().requireProvider();
            reservedCurrency = parseMoney(currencyInput, provider);
        }
        if (domain == EscrowRepository.EscrowDomain.AUCTION) {
            provider = KernelServices.economy().requireProvider();
            price = parseMoney(requiredField(control, "price"), provider);
        }
        if (items.isEmpty() && reservedCurrency == 0L
                || domain == EscrowRepository.EscrowDomain.AUCTION && items.isEmpty()) {
            return ActionResult.failure(
                    ActionResult.ReasonCode.INVALID_INPUT,
                    "escrow requires server captured item or currency value");
        }
        if (domain == EscrowRepository.EscrowDomain.TRADE
                && !Boolean.parseBoolean(field(control, "ready", "false"))) {
            return ActionResult.failure(
                    ActionResult.ReasonCode.POLICY_DENIED,
                    "trade offer must be ready before escrow publication");
        }

        String itemDigest = EscrowRepository.itemDigest(items);
        String submittedHash = field(control, "offer_hash", "");
        if (domain == EscrowRepository.EscrowDomain.TRADE
                && !submittedHash.isBlank()
                && !submittedHash.equalsIgnoreCase(itemDigest)) {
            return ActionResult.failure(
                    ActionResult.ReasonCode.CONFLICT,
                    "trade offer hash does not match the server captured items");
        }
        Instant now = Instant.now();
        Instant expiry = expiry(control, now);
        String saleType = domain == EscrowRepository.EscrowDomain.AUCTION
                ? field(control, "sale_type", "buy_now").toLowerCase(Locale.ROOT)
                : "";
        String source = domain == EscrowRepository.EscrowDomain.LOST_FOUND
                ? requiredField(control, "source") + "/" + requiredField(control, "source_reference")
                : control.featureId();
        if (domain == EscrowRepository.EscrowDomain.LOST_FOUND
                && repository.findBySource(domain, source).isPresent()) {
            return ActionResult.failure(
                    ActionResult.ReasonCode.CONFLICT,
                    "lost and found source was already recorded");
        }
        EscrowRepository.EscrowRecord prepared = new EscrowRepository.EscrowRecord(
                control.id(),
                domain,
                owner.getUUID(),
                beneficiary,
                items,
                itemDigest,
                reservedCurrency,
                price,
                provider == null ? "" : provider.currency(),
                provider == null ? "" : provider.id(),
                saleType,
                EscrowRepository.EscrowState.PREPARING,
                null,
                0L,
                null,
                0L,
                null,
                null,
                source,
                now,
                expiry,
                now,
                1L,
                "reservation prepared");
        ActionResult<EscrowRepository.EscrowRecord> persisted = repository.createAndFlush(prepared);
        if (!persisted.successful()) {
            return ActionResult.failure(persisted.reason(), persisted.detail());
        }

        List<ItemStack> before = inventorySnapshot(owner.getInventory());
        try {
            removeSelections(owner, selections);
            owner.getPersistentData().putString(RESERVATION_MARKER_KEY, prepared.id().toString());
            savePlayer(owner);
        } catch (RuntimeException exception) {
            restoreInventory(owner.getInventory(), before);
            savePlayer(owner);
            repository.replaceAndFlush(
                    prepared.transition(
                            EscrowRepository.EscrowState.RETURNED,
                            owner.getUUID(),
                            "reservation failed before custody transfer",
                            Instant.now()),
                    prepared.revision(),
                    Set.of(EscrowRepository.EscrowState.PREPARING));
            return ActionResult.failure(
                    ActionResult.ReasonCode.CONFLICT,
                    "inventory changed before escrow custody was committed");
        }
        ActionResult<String> resumed = resumeReservation(prepared, owner);
        if (!resumed.successful()
                && prepared.reservedCurrency() > 0L
                && resumed.reason() != ActionResult.ReasonCode.STORAGE_ERROR) {
            restoreInventory(owner.getInventory(), before);
            savePlayer(owner);
            owner.getPersistentData().remove(RESERVATION_MARKER_KEY);
            savePlayer(owner);
            repository.find(prepared.id()).ifPresent(current -> repository.replaceAndFlush(
                    current.transition(
                            EscrowRepository.EscrowState.RETURNED,
                            owner.getUUID(),
                            "currency reservation failed and items were restored",
                            Instant.now()),
                    current.revision(),
                    Set.of(
                            EscrowRepository.EscrowState.PREPARING,
                            EscrowRepository.EscrowState.RECOVERY_REQUIRED)));
        }
        return resumed;
    }

    private ActionResult<String> resumeReservation(
            EscrowRepository.EscrowRecord record,
            ServerPlayer owner
    ) {
        if (record.state() == EscrowRepository.EscrowState.HELD) {
            clearMarker(owner, RESERVATION_MARKER_KEY, record.id());
            return ActionResult.success(record.domain().name().toLowerCase(Locale.ROOT)
                    + " escrow held, id " + record.id());
        }
        if (record.state() != EscrowRepository.EscrowState.PREPARING
                && record.state() != EscrowRepository.EscrowState.RECOVERY_REQUIRED) {
            return ActionResult.failure(ActionResult.ReasonCode.CONFLICT, "escrow is not preparing");
        }
        if (!record.id().toString().equals(owner.getPersistentData().getString(RESERVATION_MARKER_KEY))) {
            return ActionResult.failure(
                    ActionResult.ReasonCode.CONFLICT,
                    "escrow reservation marker is unavailable");
        }
        if (record.reservedCurrency() > 0L) {
            ActionResult<EconomyProvider.Transaction> reserved = reserveCurrency(record);
            if (!reserved.successful()) {
                return ActionResult.failure(reserved.reason(), reserved.detail());
            }
        }
        EscrowRepository.EscrowRecord held = record.transition(
                EscrowRepository.EscrowState.HELD,
                owner.getUUID(),
                "item and currency custody committed",
                Instant.now());
        ActionResult<EscrowRepository.EscrowRecord> persisted = repository.replaceAndFlush(
                held,
                record.revision(),
                Set.of(
                        EscrowRepository.EscrowState.PREPARING,
                        EscrowRepository.EscrowState.RECOVERY_REQUIRED));
        if (!persisted.successful()) {
            return ActionResult.failure(persisted.reason(), persisted.detail());
        }
        clearMarker(owner, RESERVATION_MARKER_KEY, record.id());
        return ActionResult.success(record.domain().name().toLowerCase(Locale.ROOT)
                + " escrow held, id " + record.id());
    }

    private synchronized ActionResult<String> claim(
            ServerControlRepository.ControlRecord control,
            ServerPlayer claimant
    ) {
        UUID escrowId = escrowId(control);
        EscrowRepository.EscrowRecord record = repository.find(escrowId).orElse(null);
        if (record == null) {
            return ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "escrow record not found");
        }
        if (record.state() == EscrowRepository.EscrowState.SETTLED) {
            return record.settlementActorId() != null
                    && record.settlementActorId().equals(claimant.getUUID())
                    ? ActionResult.success("escrow was already settled")
                    : ActionResult.failure(ActionResult.ReasonCode.CONFLICT, "escrow was already settled");
        }
        if (record.state() == EscrowRepository.EscrowState.FROZEN) {
            return ActionResult.failure(ActionResult.ReasonCode.POLICY_DENIED, "escrow is frozen");
        }
        if (record.state() == EscrowRepository.EscrowState.SETTLING) {
            if (!claimant.getUUID().equals(record.settlementActorId())) {
                return ActionResult.failure(ActionResult.ReasonCode.CONFLICT, "escrow settlement is already pending");
            }
            if (claimant.getPersistentData().getString(DELIVERY_MARKER_KEY).isBlank()) {
                ActionResult<EscrowRepository.EscrowRecord> reset = repository.replaceAndFlush(
                        record.transition(
                                EscrowRepository.EscrowState.HELD,
                                claimant.getUUID(),
                                "uncommitted settlement released for retry",
                                Instant.now()),
                        record.revision(),
                        Set.of(EscrowRepository.EscrowState.SETTLING));
                if (!reset.successful()) {
                    return ActionResult.failure(reset.reason(), reset.detail());
                }
                record = reset.value();
                if (record.domain() == EscrowRepository.EscrowDomain.AUCTION) {
                    return beginAuctionSettlement(control, record, claimant);
                }
                return beginDelivery(record, claimant, false);
            }
            return resumeDelivery(record, claimant, false);
        }
        if (record.state() != EscrowRepository.EscrowState.HELD) {
            return ActionResult.failure(ActionResult.ReasonCode.CONFLICT, "escrow is not claimable");
        }
        if (record.domain() == EscrowRepository.EscrowDomain.AUCTION) {
            return beginAuctionSettlement(control, record, claimant);
        }
        if (!record.beneficiaryId().equals(claimant.getUUID())) {
            return ActionResult.failure(ActionResult.ReasonCode.PERMISSION_DENIED, "escrow belongs to another player");
        }
        return beginDelivery(record, claimant, false);
    }

    private ActionResult<String> beginAuctionSettlement(
            ServerControlRepository.ControlRecord control,
            EscrowRepository.EscrowRecord record,
            ServerPlayer buyer
    ) {
        long amount;
        ActionResult<List<ItemStack>> preflight = preflightDelivery(record, buyer);
        if (!preflight.successful()) {
            return ActionResult.failure(preflight.reason(), preflight.detail());
        }
        if ("buy_now".equals(record.saleType())) {
            if (!record.expiresAt().isAfter(Instant.now())) {
                return ActionResult.failure(ActionResult.ReasonCode.CONFLICT, "auction listing expired");
            }
            amount = record.price();
        } else {
            if (record.expiresAt().isAfter(Instant.now())
                    && !"settle".equalsIgnoreCase(field(control, "operation", ""))) {
                return ActionResult.failure(ActionResult.ReasonCode.POLICY_DENIED, "bid auction has not closed");
            }
            if (record.highestBidderId() == null
                    || !record.highestBidderId().equals(buyer.getUUID())) {
                return ActionResult.failure(
                        ActionResult.ReasonCode.PERMISSION_DENIED,
                        "only the winning bidder can claim this auction");
            }
            amount = record.highestBid();
        }
        EscrowRepository.EscrowRecord settling = record.transition(
                EscrowRepository.EscrowState.SETTLING,
                buyer.getUUID(),
                "auction settlement prepared",
                Instant.now());
        ActionResult<EscrowRepository.EscrowRecord> prepared = repository.replaceAndFlush(
                settling,
                record.revision(),
                Set.of(EscrowRepository.EscrowState.HELD));
        if (!prepared.successful()) {
            return ActionResult.failure(prepared.reason(), prepared.detail());
        }
        if ("buy_now".equals(record.saleType())) {
            ActionResult<EconomyProvider.Transaction> reserved = transfer(
                    settling,
                    buyer.getUUID(),
                    ESCROW_ACCOUNT_ID,
                    amount,
                    "auction purchase reservation",
                    "purchase." + settling.id() + "." + buyer.getUUID());
            if (!reserved.successful()) {
                repository.replaceAndFlush(
                        settling.transition(
                                EscrowRepository.EscrowState.HELD,
                                buyer.getUUID(),
                                "auction purchase reservation failed",
                                Instant.now()),
                        settling.revision(),
                        Set.of(EscrowRepository.EscrowState.SETTLING));
                return ActionResult.failure(reserved.reason(), reserved.detail());
            }
        }
        return deliverPrepared(settling, buyer, false);
    }

    private synchronized ActionResult<String> bid(
            ServerControlRepository.ControlRecord control,
            ServerPlayer bidder
    ) {
        UUID escrowId = escrowId(control);
        EscrowRepository.EscrowRecord record = repository.find(escrowId).orElse(null);
        if (record == null) {
            return ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "auction escrow not found");
        }
        if (record.domain() != EscrowRepository.EscrowDomain.AUCTION
                || !"bid".equals(record.saleType())) {
            return ActionResult.failure(ActionResult.ReasonCode.INVALID_INPUT, "listing does not accept bids");
        }
        if (record.state() == EscrowRepository.EscrowState.BIDDING) {
            if (!control.id().equals(record.pendingBidOperationId())
                    || !bidder.getUUID().equals(record.pendingBidderId())) {
                return ActionResult.failure(ActionResult.ReasonCode.CONFLICT, "another bid is being settled");
            }
            ActionResult<EscrowRepository.EscrowRecord> completed = completeBid(record);
            return completed.successful()
                    ? ActionResult.success("auction bid reserved")
                    : ActionResult.failure(completed.reason(), completed.detail());
        }
        if (record.state() != EscrowRepository.EscrowState.HELD
                || !record.expiresAt().isAfter(Instant.now())) {
            return ActionResult.failure(ActionResult.ReasonCode.CONFLICT, "auction is not open for bids");
        }
        if (record.ownerId().equals(bidder.getUUID())) {
            return ActionResult.failure(ActionResult.ReasonCode.POLICY_DENIED, "seller cannot bid on own listing");
        }
        EconomyProvider provider = provider(record);
        long amount = parseMoney(requiredField(control, "amount"), provider);
        long minimum = record.highestBid() == 0L
                ? record.price()
                : Math.addExact(record.highestBid(), 1L);
        if (amount < minimum) {
            return ActionResult.failure(ActionResult.ReasonCode.POLICY_DENIED, "bid is below the required minimum");
        }
        EscrowRepository.EscrowRecord bidding =
                record.beginBid(bidder.getUUID(), amount, control.id(), Instant.now());
        ActionResult<EscrowRepository.EscrowRecord> prepared = repository.replaceAndFlush(
                bidding,
                record.revision(),
                Set.of(EscrowRepository.EscrowState.HELD));
        if (!prepared.successful()) {
            return ActionResult.failure(prepared.reason(), prepared.detail());
        }
        ActionResult<EscrowRepository.EscrowRecord> completed = completeBid(bidding);
        return completed.successful()
                ? ActionResult.success("auction bid reserved")
                : ActionResult.failure(completed.reason(), completed.detail());
    }

    private ActionResult<EscrowRepository.EscrowRecord> completeBid(
            EscrowRepository.EscrowRecord bidding
    ) {
        ActionResult<EconomyProvider.Transaction> reserved = transfer(
                bidding,
                bidding.pendingBidderId(),
                ESCROW_ACCOUNT_ID,
                bidding.pendingBid(),
                "auction bid reservation",
                "bid.reserve." + bidding.id() + "." + bidding.pendingBidOperationId());
        if (!reserved.successful()) {
            return ActionResult.failure(reserved.reason(), reserved.detail());
        }
        if (bidding.highestBid() > 0L) {
            ActionResult<EconomyProvider.Transaction> refunded = transfer(
                    bidding,
                    ESCROW_ACCOUNT_ID,
                    bidding.highestBidderId(),
                    bidding.highestBid(),
                    "auction outbid refund",
                    "bid.refund." + bidding.id() + "." + bidding.revision());
            if (!refunded.successful()) {
                return ActionResult.failure(refunded.reason(), refunded.detail());
            }
        }
        return repository.replaceAndFlush(
                bidding.completeBid(Instant.now()),
                bidding.revision(),
                Set.of(EscrowRepository.EscrowState.BIDDING));
    }

    private synchronized ActionResult<String> returnValue(
            ServerControlRepository.ControlRecord control,
            ServerPlayer actor
    ) {
        UUID escrowId = escrowId(control);
        EscrowRepository.EscrowRecord record = repository.find(escrowId).orElse(null);
        if (record == null) {
            return ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "escrow record not found");
        }
        if (!record.ownerId().equals(actor.getUUID())) {
            return ActionResult.failure(ActionResult.ReasonCode.PERMISSION_DENIED, "only the owner can return escrow");
        }
        if (record.state() == EscrowRepository.EscrowState.RETURNED) {
            return ActionResult.success("escrow was already returned");
        }
        if (record.state() == EscrowRepository.EscrowState.RETURNING) {
            return resumeDelivery(record, actor, true);
        }
        if (record.state() != EscrowRepository.EscrowState.HELD
                && record.state() != EscrowRepository.EscrowState.FROZEN) {
            return ActionResult.failure(ActionResult.ReasonCode.CONFLICT, "escrow cannot be returned");
        }
        if (record.domain() == EscrowRepository.EscrowDomain.AUCTION
                && record.highestBidderId() != null
                && record.expiresAt().isAfter(Instant.now())) {
            return ActionResult.failure(
                    ActionResult.ReasonCode.POLICY_DENIED,
                    "auction with a reserved bid cannot be cancelled before expiry");
        }
        return returnHeld(record, actor);
    }

    private ActionResult<String> returnHeld(
            EscrowRepository.EscrowRecord record,
            ServerPlayer owner
    ) {
        return beginDelivery(record, owner, true);
    }

    private ActionResult<String> beginDelivery(
            EscrowRepository.EscrowRecord record,
            ServerPlayer target,
            boolean returning
    ) {
        ActionResult<List<ItemStack>> preflight = preflightDelivery(record, target);
        if (!preflight.successful()) {
            return ActionResult.failure(preflight.reason(), preflight.detail());
        }
        EscrowRepository.EscrowState next = returning
                ? EscrowRepository.EscrowState.RETURNING
                : EscrowRepository.EscrowState.SETTLING;
        EscrowRepository.EscrowRecord prepared = record.transition(
                next,
                target.getUUID(),
                returning ? "escrow return prepared" : "escrow claim prepared",
                Instant.now());
        ActionResult<EscrowRepository.EscrowRecord> persisted = repository.replaceAndFlush(
                prepared,
                record.revision(),
                Set.of(
                        EscrowRepository.EscrowState.HELD,
                        EscrowRepository.EscrowState.FROZEN));
        if (!persisted.successful()) {
            return ActionResult.failure(persisted.reason(), persisted.detail());
        }
        return deliverPrepared(prepared, target, returning);
    }

    private ActionResult<String> deliverPrepared(
            EscrowRepository.EscrowRecord prepared,
            ServerPlayer target,
            boolean returning
    ) {
        List<ItemStack> items;
        try {
            items = ItemStackSnapshotCodec.decodeStacks(prepared.items(), target.registryAccess());
        } catch (IllegalArgumentException exception) {
            return markRecovery(prepared, target.getUUID(), "escrow item registry data is unavailable");
        }
        if (!canFit(target, items)) {
            return ActionResult.failure(ActionResult.ReasonCode.POLICY_DENIED, "inventory has insufficient space");
        }
        List<ItemStack> before = inventorySnapshot(target.getInventory());
        for (ItemStack item : items) {
            ItemStack remaining = item.copy();
            if (!target.getInventory().add(remaining) || !remaining.isEmpty()) {
                restoreInventory(target.getInventory(), before);
                return ActionResult.failure(ActionResult.ReasonCode.CONFLICT, "inventory changed during settlement");
            }
        }
        target.getPersistentData().putString(DELIVERY_MARKER_KEY, prepared.id().toString());
        target.getInventory().setChanged();
        target.containerMenu.broadcastChanges();
        savePlayer(target);
        return finishDelivery(prepared, target, returning);
    }

    private ActionResult<List<ItemStack>> preflightDelivery(
            EscrowRepository.EscrowRecord record,
            ServerPlayer target
    ) {
        final List<ItemStack> items;
        try {
            items = ItemStackSnapshotCodec.decodeStacks(record.items(), target.registryAccess());
        } catch (IllegalArgumentException exception) {
            ActionResult<String> recovery =
                    markRecovery(record, target.getUUID(), "escrow item registry data is unavailable");
            return ActionResult.failure(recovery.reason(), recovery.detail());
        }
        return canFit(target, items)
                ? ActionResult.success(items)
                : ActionResult.failure(
                        ActionResult.ReasonCode.POLICY_DENIED,
                        "inventory has insufficient space");
    }

    private ActionResult<String> resumeDelivery(
            EscrowRepository.EscrowRecord record,
            ServerPlayer target,
            boolean returning
    ) {
        if (!record.id().toString().equals(target.getPersistentData().getString(DELIVERY_MARKER_KEY))) {
            return ActionResult.failure(ActionResult.ReasonCode.CONFLICT, "escrow delivery marker is unavailable");
        }
        return finishDelivery(record, target, returning);
    }

    private ActionResult<String> finishDelivery(
            EscrowRepository.EscrowRecord record,
            ServerPlayer target,
            boolean returning
    ) {
        ActionResult<EconomyProvider.Transaction> currency = settleCurrency(record, target, returning);
        if (!currency.successful()) {
            return ActionResult.failure(currency.reason(), currency.detail());
        }
        EscrowRepository.EscrowState terminal = returning
                ? EscrowRepository.EscrowState.RETURNED
                : EscrowRepository.EscrowState.SETTLED;
        EscrowRepository.EscrowRecord settled = record.transition(
                terminal,
                target.getUUID(),
                returning ? "escrow value returned" : "escrow value settled",
                Instant.now());
        ActionResult<EscrowRepository.EscrowRecord> persisted = repository.replaceAndFlush(
                settled,
                record.revision(),
                Set.of(record.state()));
        if (!persisted.successful()) {
            return ActionResult.failure(persisted.reason(), persisted.detail());
        }
        clearMarker(target, DELIVERY_MARKER_KEY, record.id());
        return ActionResult.success(returning ? "escrow value returned" : "escrow value settled");
    }

    private synchronized ActionResult<String> freeze(
            ServerControlRepository.ControlRecord control,
            UUID actorId
    ) {
        UUID escrowId = escrowId(control);
        EscrowRepository.EscrowRecord record = repository.find(escrowId).orElse(null);
        if (record == null) {
            return ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "escrow record not found");
        }
        if (record.state() == EscrowRepository.EscrowState.FROZEN) {
            return ActionResult.success("escrow was already frozen");
        }
        if (record.state() != EscrowRepository.EscrowState.HELD) {
            return ActionResult.failure(ActionResult.ReasonCode.CONFLICT, "escrow cannot be frozen");
        }
        ActionResult<EscrowRepository.EscrowRecord> result = repository.replaceAndFlush(
                record.transition(
                        EscrowRepository.EscrowState.FROZEN,
                        actorId,
                        field(control, "reason", "administrative freeze"),
                        Instant.now()),
                record.revision(),
                Set.of(EscrowRepository.EscrowState.HELD));
        return result.successful()
                ? ActionResult.success("escrow frozen")
                : ActionResult.failure(result.reason(), result.detail());
    }

    private synchronized ActionResult<String> unfreeze(
            ServerControlRepository.ControlRecord control,
            UUID actorId
    ) {
        UUID escrowId = escrowId(control);
        EscrowRepository.EscrowRecord record = repository.find(escrowId).orElse(null);
        if (record == null) {
            return ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "escrow record not found");
        }
        if (record.state() == EscrowRepository.EscrowState.HELD) {
            return ActionResult.success("escrow was already released");
        }
        if (record.state() != EscrowRepository.EscrowState.FROZEN) {
            return ActionResult.failure(ActionResult.ReasonCode.CONFLICT, "escrow is not frozen");
        }
        ActionResult<EscrowRepository.EscrowRecord> result = repository.replaceAndFlush(
                record.transition(
                        EscrowRepository.EscrowState.HELD,
                        actorId,
                        "administrative freeze released",
                        Instant.now()),
                record.revision(),
                Set.of(EscrowRepository.EscrowState.FROZEN));
        return result.successful()
                ? ActionResult.success("escrow released")
                : ActionResult.failure(result.reason(), result.detail());
    }

    private ActionResult<String> watch(
            ServerControlRepository.ControlRecord control,
            UUID actorId,
            boolean watched
    ) {
        UUID escrowId = escrowId(control);
        EscrowRepository.EscrowRecord record = repository.find(escrowId).orElse(null);
        if (record == null || record.domain() != EscrowRepository.EscrowDomain.AUCTION) {
            return ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "auction escrow not found");
        }
        if (watched) {
            KernelServices.communityState().put(
                    "auction_watch",
                    actorId,
                    record.ownerId(),
                    escrowId.toString(),
                    "watch",
                    record.expiresAt().plusSeconds(604_800L));
            return ActionResult.success("auction watch added");
        }
        KernelServices.communityState().remove("auction_watch", actorId, escrowId.toString());
        return ActionResult.success("auction watch removed");
    }

    private ActionResult<Boolean> reconcileReservationMarker(ServerPlayer player) {
        String marker = player.getPersistentData().getString(RESERVATION_MARKER_KEY);
        if (marker.isBlank()) {
            return ActionResult.success(false);
        }
        UUID recordId;
        try {
            recordId = UUID.fromString(marker);
        } catch (IllegalArgumentException exception) {
            return ActionResult.failure(ActionResult.ReasonCode.INVALID_DEFINITION, "escrow reservation marker is invalid");
        }
        EscrowRepository.EscrowRecord record = repository.find(recordId).orElse(null);
        if (record == null || !record.ownerId().equals(player.getUUID())) {
            return ActionResult.failure(
                    ActionResult.ReasonCode.INVALID_DEFINITION,
                    "escrow reservation journal is unavailable");
        }
        if (record.state() == EscrowRepository.EscrowState.HELD
                || !record.state().holdsValue()) {
            clearMarker(player, RESERVATION_MARKER_KEY, recordId);
            return ActionResult.success(true);
        }
        ActionResult<String> resumed = resumeReservation(record, player);
        return resumed.successful()
                ? ActionResult.success(true)
                : ActionResult.failure(resumed.reason(), resumed.detail());
    }

    private ActionResult<Boolean> reconcileDeliveryMarker(ServerPlayer player) {
        String marker = player.getPersistentData().getString(DELIVERY_MARKER_KEY);
        if (marker.isBlank()) {
            return ActionResult.success(false);
        }
        UUID recordId;
        try {
            recordId = UUID.fromString(marker);
        } catch (IllegalArgumentException exception) {
            return ActionResult.failure(ActionResult.ReasonCode.INVALID_DEFINITION, "escrow delivery marker is invalid");
        }
        EscrowRepository.EscrowRecord record = repository.find(recordId).orElse(null);
        if (record == null) {
            return ActionResult.failure(ActionResult.ReasonCode.INVALID_DEFINITION, "escrow delivery journal is unavailable");
        }
        if (record.state() == EscrowRepository.EscrowState.SETTLED
                || record.state() == EscrowRepository.EscrowState.RETURNED) {
            clearMarker(player, DELIVERY_MARKER_KEY, recordId);
            return ActionResult.success(true);
        }
        boolean returning = record.state() == EscrowRepository.EscrowState.RETURNING;
        if (!returning && record.state() != EscrowRepository.EscrowState.SETTLING
                || returning && !record.ownerId().equals(player.getUUID())
                || !returning && !player.getUUID().equals(record.settlementActorId())) {
            return ActionResult.failure(ActionResult.ReasonCode.CONFLICT, "escrow delivery journal conflicts with player state");
        }
        ActionResult<String> resumed = resumeDelivery(record, player, returning);
        return resumed.successful()
                ? ActionResult.success(true)
                : ActionResult.failure(resumed.reason(), resumed.detail());
    }

    private ActionResult<EconomyProvider.Transaction> reserveCurrency(
            EscrowRepository.EscrowRecord record
    ) {
        return transfer(
                record,
                record.ownerId(),
                ESCROW_ACCOUNT_ID,
                record.reservedCurrency(),
                "escrow currency reservation",
                "reserve." + record.id());
    }

    private ActionResult<EconomyProvider.Transaction> settleCurrency(
            EscrowRepository.EscrowRecord record,
            ServerPlayer target,
            boolean returning
    ) {
        long amount;
        UUID recipient;
        String key;
        String reason;
        if (record.domain() == EscrowRepository.EscrowDomain.AUCTION) {
            amount = "buy_now".equals(record.saleType()) ? record.price() : record.highestBid();
            if (returning) {
                if (record.highestBidderId() == null || amount == 0L) {
                    return ActionResult.success(null);
                }
                recipient = record.highestBidderId();
                key = "auction.refund." + record.id();
                reason = "auction cancellation refund";
            } else {
                recipient = record.ownerId();
                key = "auction.release." + record.id();
                reason = "auction seller settlement";
            }
        } else {
            amount = record.reservedCurrency();
            recipient = returning ? record.ownerId() : target.getUUID();
            key = (returning ? "refund." : "release.") + record.id();
            reason = returning ? "escrow currency refund" : "escrow currency release";
        }
        if (amount == 0L) {
            return ActionResult.success(null);
        }
        return transfer(record, ESCROW_ACCOUNT_ID, recipient, amount, reason, key);
    }

    private ActionResult<EconomyProvider.Transaction> transfer(
            EscrowRepository.EscrowRecord record,
            UUID sourceId,
            UUID targetId,
            long amount,
            String reason,
            String key
    ) {
        EconomyProvider provider = provider(record);
        ActionResult<EconomyProvider.Account> account = ensureEscrowAccount(provider, record.ownerId());
        if (!account.successful()) {
            return ActionResult.failure(account.reason(), account.detail());
        }
        ActionResult<EconomyProvider.Account> targetAccount =
                ensureAccount(provider, targetId, record.ownerId());
        if (!targetAccount.successful()) {
            return ActionResult.failure(targetAccount.reason(), targetAccount.detail());
        }
        ActionResult<EconomyProvider.Transaction> result = provider.transfer(
                new EconomyProvider.TransferRequest(
                        "escrow." + key,
                        record.ownerId(),
                        sourceId,
                        targetId,
                        reason,
                        provider.currency(),
                        amount,
                        Map.of(
                                "escrow_id", record.id().toString(),
                                "domain", record.domain().name().toLowerCase(Locale.ROOT)),
                        false));
        if (!result.successful()) {
            return result;
        }
        var flushed = KernelServices.storage().flush();
        if (!flushed.successful()) {
            return ActionResult.failure(
                    ActionResult.ReasonCode.STORAGE_ERROR,
                    "economy transaction journal could not be flushed");
        }
        return result;
    }

    private static ActionResult<EconomyProvider.Account> ensureEscrowAccount(
            EconomyProvider provider,
            UUID actorId
    ) {
        EconomyProvider.Account existing = provider.account(ESCROW_ACCOUNT_ID).orElse(null);
        if (existing != null) {
            return ActionResult.success(existing);
        }
        ActionResult<EconomyProvider.Account> created =
                provider.createAccount(new EconomyProvider.MutationRequest(
                "escrow.account.create",
                actorId,
                ESCROW_ACCOUNT_ID,
                "escrow custody account",
                provider.currency(),
                0L,
                Map.of("purpose", "escrow"),
                false));
        if (!created.successful() && provider.account(ESCROW_ACCOUNT_ID).isPresent()) {
            return ActionResult.success(provider.account(ESCROW_ACCOUNT_ID).orElseThrow());
        }
        return created;
    }

    private static ActionResult<EconomyProvider.Account> ensureAccount(
            EconomyProvider provider,
            UUID accountId,
            UUID actorId
    ) {
        EconomyProvider.Account existing = provider.account(accountId).orElse(null);
        if (existing != null) {
            return ActionResult.success(existing);
        }
        ActionResult<EconomyProvider.Account> created =
                provider.createAccount(new EconomyProvider.MutationRequest(
                        "escrow.account.create." + accountId,
                        actorId,
                        accountId,
                        "escrow settlement account",
                        provider.currency(),
                        0L,
                        Map.of("purpose", "escrow_settlement"),
                        false));
        if (!created.successful() && provider.account(accountId).isPresent()) {
            return ActionResult.success(provider.account(accountId).orElseThrow());
        }
        return created;
    }

    private static EconomyProvider provider(EscrowRepository.EscrowRecord record) {
        EconomyProvider provider = KernelServices.economy().requireProvider();
        if (!provider.id().equals(record.providerId())
                || !provider.currency().equals(record.currency())) {
            throw new IllegalStateException("escrow economy provider changed");
        }
        return provider;
    }

    private ActionResult<String> markRecovery(
            EscrowRepository.EscrowRecord record,
            UUID actorId,
            String detail
    ) {
        ActionResult<EscrowRepository.EscrowRecord> result = repository.replaceAndFlush(
                record.transition(
                        EscrowRepository.EscrowState.RECOVERY_REQUIRED,
                        actorId,
                        detail,
                        Instant.now()),
                record.revision(),
                Set.of(record.state()));
        return result.successful()
                ? ActionResult.failure(ActionResult.ReasonCode.INVALID_DEFINITION, detail)
                : ActionResult.failure(result.reason(), result.detail());
    }

    private static UUID beneficiary(
            ServerControlRepository.ControlRecord control,
            EscrowRepository.EscrowDomain domain,
            UUID ownerId
    ) {
        String candidate = switch (domain) {
            case PARCEL -> field(control, "recipient", "");
            case LOST_FOUND -> field(control, "claimant", "");
            case TRADE -> field(control, "partner", "");
            case AUCTION -> "";
        };
        if (candidate.isBlank()) {
            if (domain == EscrowRepository.EscrowDomain.LOST_FOUND
                    || domain == EscrowRepository.EscrowDomain.AUCTION) {
                return ownerId;
            }
            throw new IllegalArgumentException("escrow beneficiary is required");
        }
        UUID result = parseUuid(candidate, "escrow beneficiary is invalid");
        if (result.equals(ownerId)
                && (domain == EscrowRepository.EscrowDomain.PARCEL
                || domain == EscrowRepository.EscrowDomain.TRADE)) {
            throw new IllegalArgumentException("self transfer is not allowed");
        }
        return result;
    }

    private static boolean blockedBy(
            UUID ownerId,
            UUID subjectId,
            String interaction
    ) {
        return CommunityCommands.interactionBlocked(ownerId, subjectId, interaction)
                || KernelServices.serverControls().records("interaction_blocks").stream()
                .filter(record -> record.state() == ServerControlRepository.RecordState.ACTIVE)
                .filter(record -> record.ownerId().equals(ownerId))
                .filter(record -> Boolean.parseBoolean(field(record, "blocked", "true")))
                .filter(record -> {
                    String scope = field(record, "interaction", "all");
                    return scope.equals("all") || scope.equals(interaction);
                })
                .anyMatch(record -> subjectId.equals(record.subjectId())
                        || subjectId.toString().equals(field(record, "player", "")));
    }

    private static UUID escrowId(ServerControlRepository.ControlRecord control) {
        return parseUuid(requiredField(control, "escrow_id"), "escrow id is invalid");
    }

    private static UUID parseUuid(String value, String detail) {
        try {
            return UUID.fromString(value.strip());
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(detail);
        }
    }

    private static long parseMoney(String value, EconomyProvider provider) {
        return EconomyMoney.parsePositive(
                value,
                provider.minorUnits(),
                KernelServices.economy().settings().maximumTransaction());
    }

    private static Instant expiry(
            ServerControlRepository.ControlRecord control,
            Instant now
    ) {
        String value = field(control, "expires_at", "");
        Instant result;
        try {
            result = value.isBlank()
                    ? control.expiresAt() == null
                    ? now.plusSeconds(DEFAULT_EXPIRY_SECONDS)
                    : control.expiresAt()
                    : Instant.parse(value);
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException("escrow expiry is invalid");
        }
        if (!result.isAfter(now) || result.isAfter(now.plusSeconds(31_536_000L))) {
            throw new IllegalArgumentException("escrow expiry is outside bounds");
        }
        return result;
    }

    private static List<Selection> parseSelections(
            ServerPlayer player,
            String value
    ) {
        if (value == null || value.isBlank()) {
            return List.of();
        }
        String[] entries = value.split(",", -1);
        if (entries.length > MAXIMUM_SELECTIONS) {
            throw new IllegalArgumentException("item selection count is outside bounds");
        }
        Set<Integer> occupied = new HashSet<>();
        List<Selection> selections = new ArrayList<>();
        for (String raw : entries) {
            String entry = raw.strip().toLowerCase(Locale.ROOT);
            if (entry.isBlank()) {
                throw new IllegalArgumentException("item selection is empty");
            }
            String[] parts = entry.split(":", -1);
            if (parts.length > 2) {
                throw new IllegalArgumentException("item selection syntax is invalid");
            }
            int slot;
            try {
                slot = "hand".equals(parts[0]) ? player.getInventory().selected : Integer.parseInt(parts[0]);
            } catch (NumberFormatException exception) {
                throw new IllegalArgumentException("item slot is invalid");
            }
            if (slot < 0
                    || slot >= player.getInventory().getContainerSize()
                    || !occupied.add(slot)) {
                throw new IllegalArgumentException("item slot is outside bounds or duplicated");
            }
            ItemStack current = player.getInventory().getItem(slot);
            if (current.isEmpty()) {
                throw new IllegalArgumentException("selected item slot is empty");
            }
            int amount;
            try {
                amount = parts.length == 1 || parts[1].isBlank()
                        ? current.getCount()
                        : Integer.parseInt(parts[1]);
            } catch (NumberFormatException exception) {
                throw new IllegalArgumentException("item amount is invalid");
            }
            if (amount < 1 || amount > current.getCount()) {
                throw new IllegalArgumentException("item amount is outside bounds");
            }
            selections.add(new Selection(slot, amount, current.copy()));
        }
        return List.copyOf(selections);
    }

    private static List<ItemStack> captureSelections(
            ServerPlayer player,
            List<Selection> selections
    ) {
        List<ItemStack> result = new ArrayList<>(selections.size());
        for (Selection selection : selections) {
            ItemStack current = player.getInventory().getItem(selection.slot());
            if (!ItemStack.isSameItemSameComponents(current, selection.expected())
                    || current.getCount() < selection.amount()) {
                throw new IllegalArgumentException("selected inventory changed");
            }
            ItemStack captured = current.copy();
            captured.setCount(selection.amount());
            result.add(captured);
        }
        return List.copyOf(result);
    }

    private static void removeSelections(
            ServerPlayer player,
            List<Selection> selections
    ) {
        for (Selection selection : selections) {
            ItemStack current = player.getInventory().getItem(selection.slot());
            if (!ItemStack.isSameItemSameComponents(current, selection.expected())
                    || current.getCount() < selection.amount()) {
                throw new IllegalStateException("selected inventory changed");
            }
        }
        for (Selection selection : selections) {
            ItemStack current = player.getInventory().getItem(selection.slot());
            current.shrink(selection.amount());
            if (current.isEmpty()) {
                player.getInventory().setItem(selection.slot(), ItemStack.EMPTY);
            }
        }
        player.getInventory().setChanged();
        player.containerMenu.broadcastChanges();
    }

    private static boolean canFit(ServerPlayer player, List<ItemStack> items) {
        Inventory simulated = new Inventory(player);
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            simulated.setItem(slot, player.getInventory().getItem(slot).copy());
        }
        for (ItemStack item : items) {
            ItemStack remaining = item.copy();
            if (!simulated.add(remaining) || !remaining.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private static List<ItemStack> inventorySnapshot(Inventory inventory) {
        List<ItemStack> result = new ArrayList<>(inventory.getContainerSize());
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            result.add(inventory.getItem(slot).copy());
        }
        return result;
    }

    private static void restoreInventory(Inventory inventory, List<ItemStack> snapshot) {
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            inventory.setItem(slot, snapshot.get(slot).copy());
        }
        inventory.setChanged();
    }

    private static void clearMarker(ServerPlayer player, String key, UUID recordId) {
        if (recordId.toString().equals(player.getPersistentData().getString(key))) {
            player.getPersistentData().remove(key);
            savePlayer(player);
        }
    }

    private static void savePlayer(ServerPlayer player) {
        player.server.getPlayerList().saveAll();
    }

    private static String requiredField(
            ServerControlRepository.ControlRecord record,
            String fieldId
    ) {
        String value = field(record, fieldId, "");
        if (value.isBlank()) {
            throw new IllegalArgumentException(fieldId + " is required for this escrow operation");
        }
        return value;
    }

    private static String field(
            ServerControlRepository.ControlRecord record,
            String fieldId,
            String fallback
    ) {
        return record.metadata().getOrDefault("field." + fieldId, fallback);
    }

    private record Selection(int slot, int amount, ItemStack expected) {
        private Selection {
            if (slot < 0 || amount < 1) {
                throw new IllegalArgumentException("item selection is invalid");
            }
            expected = Objects.requireNonNull(expected, "expected").copy();
        }
    }

    public record RecoverySummary(int completed, int released) {
        public RecoverySummary {
            if (completed < 0 || released < 0) {
                throw new IllegalArgumentException("escrow recovery counts are invalid");
            }
        }
    }
}
