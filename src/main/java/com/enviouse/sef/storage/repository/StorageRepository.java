package com.enviouse.sef.storage.repository;

import java.io.IOException;
import java.nio.file.Path;

public interface StorageRepository {
    String id();

    String domain();

    int schemaVersion();

    Path path();

    LoadResult load(Path managedRoot);

    void flush() throws IOException;

    boolean dirty();

    RepositoryState state();

    record LoadResult(RepositoryState state, String detail) {
        public LoadResult {
            detail = detail == null ? "" : detail;
        }
    }

    enum RepositoryState {
        NEW,
        READY,
        MISSING,
        RECOVERY,
        UNSUPPORTED,
        ERROR,
        CLOSED
    }
}
