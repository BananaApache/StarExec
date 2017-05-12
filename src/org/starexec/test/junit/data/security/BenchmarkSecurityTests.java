package org.starexec.test.junit.data.security;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.starexec.data.database.*;
import org.starexec.data.security.BenchmarkSecurity;
import org.starexec.data.security.GeneralSecurity;
import org.starexec.data.security.ProcessorSecurity;
import org.starexec.data.security.ValidatorStatusCode;
import org.starexec.data.to.Benchmark;
import org.starexec.data.to.BenchmarkUploadStatus;
import org.starexec.data.to.Processor;
import org.starexec.data.to.enums.ProcessorType;
import org.starexec.util.Validator;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Benchmarks.class, Permissions.class, GeneralSecurity.class})
public class BenchmarkSecurityTests {

    @Before
    public void initialize() {
        PowerMockito.mockStatic(Benchmarks.class);
        PowerMockito.mockStatic(Permissions.class);
        PowerMockito.mockStatic(GeneralSecurity.class);
    }

    @Test
    public void canUserDownloadBenchmarkGetsNullBenchTest() {
        // given
        int benchId = 1;
        int userId = 2;
        given(Benchmarks.get(benchId)).willReturn(null);

        // when / then
        assertFalse(BenchmarkSecurity.canUserDownloadBenchmark(benchId, userId).isSuccess());
    }

    @Test
    public void canUserDownloadBenchmarkWhenUserCantSeeBenchTest() {
        // given
        int benchId = 1;
        int userId = 2;
        given(Benchmarks.get(benchId)).willReturn(mock(Benchmark.class));
        given(Permissions.canUserSeeBench(benchId, userId)).willReturn(false);

        // when / then
        assertFalse(BenchmarkSecurity.canUserDownloadBenchmark(benchId, userId).isSuccess());
    }

    @Test
    public void canUserDownloadBenchmarkWhenUserHasAdminPrivileges() {
        // given
        int benchId = 1;
        int userId = 2;
        int otherUserId = 3;
        Benchmark bench = mock(Benchmark.class);
        given(bench.isDownloadable()).willReturn(false);
        // Then benchmark is not the users benchmark
        given(bench.getUserId()).willReturn(otherUserId);
        given(Benchmarks.get(benchId)).willReturn(bench);
        given(Permissions.canUserSeeBench(benchId, userId)).willReturn(true);
        // The user has admin privileges.
        given(GeneralSecurity.hasAdminReadPrivileges(userId)).willReturn(true);

        // when / then
        assertTrue("The user is an admin so should be able to download the benchmark.",
                BenchmarkSecurity.canUserDownloadBenchmark(benchId, userId).isSuccess());
    }

    @Test
    public void canUserDownloadBenchmarkWhenUserIsOwner() {
        // given
        int benchId = 1;
        int userId = 2;
        Benchmark bench = mock(Benchmark.class);
        given(bench.isDownloadable()).willReturn(false);
        // Then benchmark is not the users benchmark
        given(bench.getUserId()).willReturn(userId);
        given(Benchmarks.get(benchId)).willReturn(bench);
        given(Permissions.canUserSeeBench(benchId, userId)).willReturn(true);
        // The user has admin privileges.
        given(GeneralSecurity.hasAdminReadPrivileges(userId)).willReturn(false);

        // when / then
        assertTrue("The user is owner so should be able to download the benchmark.",
                BenchmarkSecurity.canUserDownloadBenchmark(benchId, userId).isSuccess());
    }

    @Test
    public void canUserDownloadBenchmarkWhenBenchmarkIsDownloadable() {
        // given
        int benchId = 1;
        int userId = 2;
        int otherUserId = 3;
        Benchmark bench = mock(Benchmark.class);
        given(bench.isDownloadable()).willReturn(true);
        // Then benchmark is not the users benchmark
        given(bench.getUserId()).willReturn(otherUserId);
        given(Benchmarks.get(benchId)).willReturn(bench);
        given(Permissions.canUserSeeBench(benchId, userId)).willReturn(true);
        // The user has admin privileges.
        given(GeneralSecurity.hasAdminReadPrivileges(userId)).willReturn(false);

        // when / then
        assertTrue("The benchmark is downloadable so user should be able to download.",
                BenchmarkSecurity.canUserDownloadBenchmark(benchId, userId).isSuccess());
    }
    @Test
    public void canUserDownloadBenchmarkWhenUserIsNotAdminBenchIsNotDownloadableAndUserIsNotOwner() {
        // given
        int benchId = 1;
        int userId = 2;
        int otherUserId = 3;
        Benchmark bench = mock(Benchmark.class);
        given(bench.isDownloadable()).willReturn(false);
        // Then benchmark is not the users benchmark
        given(bench.getUserId()).willReturn(otherUserId);
        given(Benchmarks.get(benchId)).willReturn(bench);
        given(Permissions.canUserSeeBench(benchId, userId)).willReturn(true);
        // The user has admin privileges.
        given(GeneralSecurity.hasAdminReadPrivileges(userId)).willReturn(false);

        // when / then
        assertFalse("The user should not be able to download the benchmark.",
                BenchmarkSecurity.canUserDownloadBenchmark(benchId, userId).isSuccess());
    }

    @Test
    public void canUserGetAnonymousLinkIfUserOwnsBenchmarkOrIsAdmin() {
        // given
        int benchId = 1;
        int userId = 2;
        Benchmark bench = mock(Benchmark.class);
        given(Benchmarks.get(benchId)).willReturn(bench);
        given(BenchmarkSecurity.userOwnsBenchOrIsAdmin(bench, userId)).willReturn(true);

        // when / then
        assertTrue("Admins and owners should be able to get the anon link.",
                BenchmarkSecurity.canUserGetAnonymousLink(benchId, userId).isSuccess());
    }

    @Test
    public void canUserGetAnonymousLinkIfUserDoesNotOwnBenchmarkOrIsAdmin() {
        // given
        int benchId = 1;
        int userId = 2;
        Benchmark bench = mock(Benchmark.class);
        given(Benchmarks.get(benchId)).willReturn(bench);
        given(BenchmarkSecurity.userOwnsBenchOrIsAdmin(bench, userId)).willReturn(false);

        // when / then
        assertFalse("Only admins and owners should be able to get the anon link.",
                BenchmarkSecurity.canUserGetAnonymousLink(benchId, userId).isSuccess());
    }

    @Test
    public void canUserSeeBenchmarkContentsWhenBenchIsNull() {
        // given
        int benchId = 1;
        int userId = 2;
        given(Benchmarks.get(benchId)).willReturn(null);
        // when / then
        assertFalse("Should be false for null benchmark.", BenchmarkSecurity.canUserSeeBenchmarkContents(benchId, userId).isSuccess());
    }

    @Test
    public void userCanSeeBenchmarkContentsIfUserIsOwner() {
        // given
        int benchId = 1;
        int userId = 2;
        Benchmark bench = mock(Benchmark.class);
        // User owns this benchmark.
        given(bench.getUserId()).willReturn(userId);
        // user is not admin
        given(GeneralSecurity.hasAdminReadPrivileges(userId)).willReturn(false);
        given(Benchmarks.get(benchId)).willReturn(bench);

        // when / then
        assertTrue("Should be true since user owns the benchmark",
                BenchmarkSecurity.canUserSeeBenchmarkContents(benchId, userId).isSuccess());
    }

    @Test
    public void userCanSeeBenchmarkContentsIfUserIsAdmin() {
        // given
        int benchId = 1;
        int userId = 2;
        int otherUserId = 3;
        Benchmark bench = mock(Benchmark.class);
        // User does not own this benchmark
        given(bench.getUserId()).willReturn(otherUserId);
        // user is admin
        given(GeneralSecurity.hasAdminReadPrivileges(userId)).willReturn(true);
        given(Benchmarks.get(benchId)).willReturn(bench);

        // when / then
        assertTrue("Should be true since user is admin", BenchmarkSecurity.canUserSeeBenchmarkContents(benchId, userId).isSuccess());
    }

    @Test
    public void userCanSeeBenchmarkContentsIfBenchIsDownloadableAndUserCanSeeBench() {
        // given
        int benchId = 1;
        int userId = 2;
        int otherUserId = 3;
        Benchmark bench = mock(Benchmark.class);
        // User does not own this benchmark
        given(bench.getUserId()).willReturn(otherUserId);
        // bench is downloadable
        given(bench.isDownloadable()).willReturn(true);
        given(bench.getId()).willReturn(benchId);
        // user is not admin.
        given(GeneralSecurity.hasAdminReadPrivileges(userId)).willReturn(false);
        given(Permissions.canUserSeeBench(benchId, userId)).willReturn(true);
        given(Benchmarks.get(benchId)).willReturn(bench);

        // when / then
        assertTrue("Should be true since bench is downloadable and user can see the benchmark.",
                BenchmarkSecurity.canUserSeeBenchmarkContents(benchId, userId).isSuccess());

    }

    @Test
    public void userCannotSeeBenchmarkContentsIfBenchIsNotDownloadable() {
        // given
        int benchId = 1;
        int userId = 2;
        int otherUserId = 3;
        Benchmark bench = mock(Benchmark.class);
        // User does not own this benchmark
        given(bench.getUserId()).willReturn(otherUserId);
        // bench is downloadable
        given(bench.isDownloadable()).willReturn(false);
        // user is not admin.
        given(GeneralSecurity.hasAdminReadPrivileges(userId)).willReturn(false);
        given(Permissions.canUserSeeBench(benchId, userId)).willReturn(true);
        given(Benchmarks.get(benchId)).willReturn(bench);

        // when / then
        assertFalse("Should be false since bench is not downloadable.",
                BenchmarkSecurity.canUserSeeBenchmarkContents(benchId, userId).isSuccess());
    }

    @Test
    public void userCannotSeeBenchmarkContentsIfUserCannotSeeBench() {

        // given
        int benchId = 1;
        int userId = 2;
        int otherUserId = 3;
        Benchmark bench = mock(Benchmark.class);
        // User does not own this benchmark
        given(bench.getUserId()).willReturn(otherUserId);
        // bench is downloadable
        given(bench.isDownloadable()).willReturn(true);
        // user is not admin.
        given(GeneralSecurity.hasAdminReadPrivileges(userId)).willReturn(false);
        given(Permissions.canUserSeeBench(benchId, userId)).willReturn(false);
        given(Benchmarks.get(benchId)).willReturn(bench);

        // when / then
        assertFalse("Should be true false since user cant see bench.",
                BenchmarkSecurity.canUserSeeBenchmarkContents(benchId, userId).isSuccess());
    }

    @Test
    public void userCannotDeleteOrRecycleBenchIfBenchCannotBeFound() {
        // given
        int benchId = 1;
        int userId = 2;
        given(Benchmarks.getIncludeDeletedAndRecycled(benchId,false)).willReturn(null);

        // when / then
        assertFalse("Benchmark cannot be found so should not be deletable.",
                BenchmarkSecurity.canUserDeleteBench(benchId, userId).isSuccess());
        assertFalse("Benchmark cannot be found so should not be recyclable.",
                BenchmarkSecurity.canUserRecycleBench(benchId, userId).isSuccess());
    }

    @Test
    public void userCannotDeleteOrRecycleBenchIfTheyDoNotOwnBenchOrAreAdmin() {
        // given
        int benchId = 1;
        int userId = 2;
        Benchmark bench = mock(Benchmark.class);
        given(Benchmarks.getIncludeDeletedAndRecycled(benchId,false)).willReturn(bench);
        given(BenchmarkSecurity.userOwnsBenchOrIsAdmin(bench, userId)).willReturn(false);

        // when / then
        assertFalse("User does not own bench and is not admin so should not be deletable.",
                BenchmarkSecurity.canUserDeleteBench(benchId, userId).isSuccess());
        assertFalse("User does not own bench and is not admin so should not be recyclable.",
                BenchmarkSecurity.canUserRecycleBench(benchId, userId).isSuccess());
    }

    @Test
    public void userCanDeleteOrRecycleBenchIfTheyOwnBenchOrAreAdmin() {
        // given
        int benchId = 1;
        int userId = 2;
        Benchmark bench = mock(Benchmark.class);
        given(Benchmarks.getIncludeDeletedAndRecycled(benchId,false)).willReturn(bench);
        given(Benchmarks.get(benchId)).willReturn(bench);
        given(BenchmarkSecurity.userOwnsBenchOrIsAdmin(bench, userId)).willReturn(true);

        // when / then
        assertTrue("User owns bench or is admin so should be deletable.",
                BenchmarkSecurity.canUserDeleteBench(benchId, userId).isSuccess());
        assertTrue("User owns bench or is admin so should be recyclable.",
                BenchmarkSecurity.canUserRecycleBench(benchId, userId).isSuccess());
    }

    @Test
    public void userCantRestoreBenchmarkIfBenchmarkCantBeFound() {
        // given
        given(Benchmarks.getIncludeDeletedAndRecycled(anyInt(), anyBoolean())).willReturn(null);

        // when
        ValidatorStatusCode status = BenchmarkSecurity.canUserRestoreBenchmark(1, 2);

        // then
        assertFalse("User cant restore benchmark if benchmark was not found.", status.isSuccess());
    }
    

    // TODO: still needs tests for:
    // canUserEditBenchmark
    // userOwnsBenchOrIsAdmin
    // canUserRecycleOrphanedBenchmarks
    // canGetJsonBenchmark
    // canUserSeeBenchmarkStatus
}
