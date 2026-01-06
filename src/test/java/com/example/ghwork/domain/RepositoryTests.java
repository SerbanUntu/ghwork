package com.example.ghwork.domain;

import static org.junit.jupiter.api.Assertions.*;

import com.example.ghwork.cli.exceptions.InvalidRepositoryStringException;
import org.junit.jupiter.api.Test;

public class RepositoryTests {

    @Test
    public void throwsWithWrongStructureTest() {
        assertThrows(InvalidRepositoryStringException.class, () -> new Repository("repo"));
        assertThrows(InvalidRepositoryStringException.class, () -> new Repository("repo/  "));
        assertThrows(InvalidRepositoryStringException.class, () -> new Repository(" /  repo"));
    }

    @Test
    public void acceptsOwnerSlashRepoTest() {
        Repository repo = new Repository("owner/repo");
        assertEquals("owner", repo.getOwner());
        assertEquals("repo", repo.getRepository());
    }
}
