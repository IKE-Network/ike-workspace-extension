package network.ike.extension;

import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.maven.AbstractMavenLifecycleParticipant;
import org.apache.maven.MavenExecutionException;
import org.apache.maven.execution.MavenSession;

/**
 * Fails a build whose wait for the tree's {@link BuildLock} timed out, and releases the lock at
 * session end should {@link BuildSessionSpy} not have.
 *
 * <p>The spy waits for the lock when project discovery starts, but an {@code EventSpy} cannot
 * fail a build; the first point after discovery that can is {@code afterProjectsRead}, so the
 * build stops there, with the holder named, before any mojo runs.
 */
@Named("ike-workspace-build-lock-gate")
@Singleton
public class BuildLockGate extends AbstractMavenLifecycleParticipant {

    /** Creates the participant; instantiated by the sisu index, never directly. */
    public BuildLockGate() {}

    @Override
    public void afterProjectsRead(MavenSession session) throws MavenExecutionException {
        if (BuildSessionSpy.state().outcome() instanceof BuildLock.TimedOut timedOut) {
            throw new MavenExecutionException(BuildLock.timedOutMessage(timedOut), (Throwable) null);
        }
    }

    @Override
    public void afterSessionEnd(MavenSession session) {
        BuildSessionSpy.release(session);
    }
}
