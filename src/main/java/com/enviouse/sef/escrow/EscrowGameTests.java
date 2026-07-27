package com.enviouse.sef.escrow;

import com.enviouse.sef.control.ServerControlExecutionService;
import com.enviouse.sef.control.ServerControlRepository;
import com.enviouse.sef.economy.EconomyProvider;
import com.enviouse.sef.kernel.ActionResult;
import com.enviouse.sef.kernel.KernelServices;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@GameTestHolder("sef")
@PrefixGameTestTemplate(false)
public final class EscrowGameTests {
    private EscrowGameTests() {
    }

    @GameTest(template = "empty")
    public static void parcelMovesServerCapturedItemsExactlyOnce(GameTestHelper helper) {
        ServerPlayer sender = helper.makeMockServerPlayerInLevel();
        ServerPlayer recipient = helper.makeMockServerPlayerInLevel();
        sender.getInventory().setItem(0, new ItemStack(Items.DIAMOND, 5));
        sender.getInventory().setChanged();
        UUID escrowId = UUID.randomUUID();

        var created = KernelServices.escrow().execute(
                control(
                        escrowId,
                        "parcels",
                        sender.getUUID(),
                        Map.of(
                                "field.operation", "create",
                                "field.recipient", recipient.getUUID().toString(),
                                "field.items", "0:3",
                                "field.expires_at", Instant.now().plusSeconds(3600L).toString())),
                context(sender));

        helper.assertTrue(created.successful(), created.detail());
        helper.assertTrue(sender.getInventory().countItem(Items.DIAMOND) == 2, "parcel did not take exact item count");
        helper.assertTrue(
                KernelServices.escrow().repository().find(escrowId).orElseThrow().state()
                        == EscrowRepository.EscrowState.HELD,
                "parcel value was not held");

        var claimed = KernelServices.escrow().execute(
                control(
                        UUID.randomUUID(),
                        "parcels",
                        recipient.getUUID(),
                        Map.of(
                                "field.operation", "accept",
                                "field.escrow_id", escrowId.toString())),
                context(recipient));
        helper.assertTrue(claimed.successful(), claimed.detail());
        helper.assertTrue(recipient.getInventory().countItem(Items.DIAMOND) == 3, "parcel was not delivered");

        var duplicate = KernelServices.escrow().execute(
                control(
                        UUID.randomUUID(),
                        "parcels",
                        recipient.getUUID(),
                        Map.of(
                                "field.operation", "accept",
                                "field.escrow_id", escrowId.toString())),
                context(recipient));
        helper.assertTrue(duplicate.successful(), duplicate.detail());
        helper.assertTrue(
                recipient.getInventory().countItem(Items.DIAMOND) == 3,
                "duplicate parcel claim duplicated items");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void parcelCurrencyUsesIdempotentCustodyAndRelease(GameTestHelper helper) {
        ServerPlayer sender = helper.makeMockServerPlayerInLevel();
        ServerPlayer recipient = helper.makeMockServerPlayerInLevel();
        EconomyProvider provider = KernelServices.economy().requireProvider();
        var opened = provider.createAccount(new EconomyProvider.MutationRequest(
                "escrow.gametest.sender." + sender.getUUID(),
                sender.getUUID(),
                sender.getUUID(),
                "escrow game test account",
                provider.currency(),
                1_000L,
                Map.of(),
                false));
        helper.assertTrue(opened.successful(), opened.detail());
        UUID escrowId = UUID.randomUUID();

        var created = KernelServices.escrow().execute(
                control(
                        escrowId,
                        "parcels",
                        sender.getUUID(),
                        Map.of(
                                "field.operation", "create",
                                "field.recipient", recipient.getUUID().toString(),
                                "field.currency", "2.50",
                                "field.expires_at", Instant.now().plusSeconds(3600L).toString())),
                context(sender));
        helper.assertTrue(created.successful(), created.detail());
        helper.assertTrue(
                provider.account(sender.getUUID()).orElseThrow().balance() == 750L,
                "parcel currency was not reserved");

        var claimed = KernelServices.escrow().execute(
                control(
                        UUID.randomUUID(),
                        "parcels",
                        recipient.getUUID(),
                        Map.of(
                                "field.operation", "accept",
                                "field.escrow_id", escrowId.toString())),
                context(recipient));
        helper.assertTrue(claimed.successful(), claimed.detail());
        helper.assertTrue(
                provider.account(recipient.getUUID()).orElseThrow().balance() == 250L,
                "parcel currency was not released");
        helper.assertTrue(
                KernelServices.escrow().repository().find(escrowId).orElseThrow().state()
                        == EscrowRepository.EscrowState.SETTLED,
                "parcel currency journal was not settled");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void parcelHonorsRecipientInteractionBlocks(GameTestHelper helper) {
        ServerPlayer sender = helper.makeMockServerPlayerInLevel();
        ServerPlayer recipient = helper.makeMockServerPlayerInLevel();
        sender.getInventory().setItem(0, new ItemStack(Items.DIAMOND, 2));
        KernelServices.communityState().put(
                "interaction_block",
                recipient.getUUID(),
                sender.getUUID(),
                sender.getUUID() + ":parcels",
                "parcels",
                null);

        var denied = KernelServices.escrow().execute(
                control(
                        UUID.randomUUID(),
                        "parcels",
                        sender.getUUID(),
                        Map.of(
                                "field.operation", "create",
                                "field.recipient", recipient.getUUID().toString(),
                                "field.items", "0:1")),
                context(sender));

        helper.assertTrue(!denied.successful(), "blocked parcel was accepted");
        helper.assertTrue(
                denied.reason() == ActionResult.ReasonCode.TARGET_DENIED,
                "blocked parcel used the wrong rejection reason");
        helper.assertTrue(
                sender.getInventory().countItem(Items.DIAMOND) == 2,
                "blocked parcel changed the sender inventory");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void lostAndFoundRejectsDuplicateTypedSources(GameTestHelper helper) {
        ServerPlayer custodian = helper.makeMockServerPlayerInLevel();
        custodian.getInventory().setItem(0, new ItemStack(Items.DIAMOND, 2));
        String sourceReference = UUID.randomUUID().toString();

        var created = KernelServices.escrow().execute(
                control(
                        UUID.randomUUID(),
                        "lost_found",
                        custodian.getUUID(),
                        Map.of(
                                "field.operation", "create",
                                "field.source", "grave",
                                "field.source_reference", sourceReference,
                                "field.items", "0:1")),
                context(custodian));
        helper.assertTrue(created.successful(), created.detail());

        var duplicate = KernelServices.escrow().execute(
                control(
                        UUID.randomUUID(),
                        "lost_found",
                        custodian.getUUID(),
                        Map.of(
                                "field.operation", "create",
                                "field.source", "grave",
                                "field.source_reference", sourceReference,
                                "field.items", "0:1")),
                context(custodian));

        helper.assertTrue(!duplicate.successful(), "duplicate lost and found source was accepted");
        helper.assertTrue(
                duplicate.reason() == ActionResult.ReasonCode.CONFLICT,
                "duplicate source used the wrong rejection reason");
        helper.assertTrue(
                custodian.getInventory().countItem(Items.DIAMOND) == 1,
                "duplicate source changed escrow custody");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void auctionWatchStateCanBeAddedAndRemoved(GameTestHelper helper) {
        ServerPlayer seller = helper.makeMockServerPlayerInLevel();
        ServerPlayer watcher = helper.makeMockServerPlayerInLevel();
        seller.getInventory().setItem(0, new ItemStack(Items.DIAMOND, 1));
        UUID escrowId = UUID.randomUUID();

        var created = KernelServices.escrow().execute(
                control(
                        escrowId,
                        "auctions",
                        seller.getUUID(),
                        Map.of(
                                "field.operation", "create",
                                "field.item", "0:1",
                                "field.price", "5.00",
                                "field.sale_type", "buy_now")),
                context(seller));
        helper.assertTrue(created.successful(), created.detail());

        var watched = KernelServices.escrow().execute(
                control(
                        UUID.randomUUID(),
                        "auctions",
                        watcher.getUUID(),
                        Map.of(
                                "field.operation", "watch",
                                "field.escrow_id", escrowId.toString())),
                context(watcher));
        helper.assertTrue(watched.successful(), watched.detail());
        helper.assertTrue(
                KernelServices.communityState().find(
                        "auction_watch",
                        watcher.getUUID(),
                        escrowId.toString()).isPresent(),
                "auction watch was not stored");

        var unwatched = KernelServices.escrow().execute(
                control(
                        UUID.randomUUID(),
                        "auctions",
                        watcher.getUUID(),
                        Map.of(
                                "field.operation", "unwatch",
                                "field.escrow_id", escrowId.toString())),
                context(watcher));
        helper.assertTrue(unwatched.successful(), unwatched.detail());
        helper.assertTrue(
                KernelServices.communityState().find(
                        "auction_watch",
                        watcher.getUUID(),
                        escrowId.toString()).isEmpty(),
                "auction watch was not removed");
        helper.succeed();
    }

    private static ServerControlRepository.ControlRecord control(
            UUID id,
            String feature,
            UUID owner,
            Map<String, String> metadata
    ) {
        Instant now = Instant.now();
        return new ServerControlRepository.ControlRecord(
                id,
                feature,
                owner,
                null,
                feature,
                "escrow game test",
                ServerControlRepository.RecordState.APPROVED,
                now,
                now,
                now.plusSeconds(3600L),
                1L,
                metadata);
    }

    private static ServerControlExecutionService.ExecutionContext context(ServerPlayer player) {
        return new ServerControlExecutionService.ExecutionContext() {
            @Override
            public Object server() {
                return player.server;
            }

            @Override
            public Object source() {
                return player.createCommandSourceStack();
            }
        };
    }
}
