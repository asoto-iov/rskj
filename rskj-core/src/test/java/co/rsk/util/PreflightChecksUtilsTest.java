package co.rsk.util;

import co.rsk.RskContext;
import org.ethereum.util.RskTestContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

/**
 * Created by Nazaret García on 22/01/2021
 */

public class PreflightChecksUtilsTest {

    @Test
    public void runChecks_receivesSkipJavaCheck_skipsJavaChecks() throws Exception {
        String[] args = {"--skip-java-check"};

        RskContext rskContext = new RskTestContext(args);
        PreflightChecksUtils preflightChecksUtilsSpy = spy(new PreflightChecksUtils(rskContext));

        when(preflightChecksUtilsSpy.getJavaVersion()).thenReturn(null);

        preflightChecksUtilsSpy.runChecks();

        verify(preflightChecksUtilsSpy, times(0)).getJavaVersion();

        rskContext.close();
    }

    @Test
    public void getIntJavaVersion_OK() {
        RskContext rskContext = new RskTestContext(new String[0]);
        PreflightChecksUtils preflightChecksUtils = new PreflightChecksUtils(rskContext);

        Assertions.assertEquals(preflightChecksUtils.getIntJavaVersion("1.8.0_275"), 8);
        Assertions.assertEquals(preflightChecksUtils.getIntJavaVersion("1.8.0_72-ea"), 8);
        Assertions.assertEquals(preflightChecksUtils.getIntJavaVersion("11.8.0_71-ea"), 11);
        Assertions.assertEquals(preflightChecksUtils.getIntJavaVersion("11.0"), 11);
        Assertions.assertEquals(preflightChecksUtils.getIntJavaVersion("9"), 9);
        Assertions.assertEquals(preflightChecksUtils.getIntJavaVersion("11"), 11);
        Assertions.assertEquals(preflightChecksUtils.getIntJavaVersion("333"), 333);
        Assertions.assertEquals(preflightChecksUtils.getIntJavaVersion("9-ea"), 9);

        rskContext.close();
    }

    @Test
    public void runChecks_invalidJavaVersion_exceptionIsThrown() throws Exception {
        RskContext rskContext = new RskTestContext(new String[0]);
        PreflightChecksUtils preflightChecksUtilsSpy = spy(new PreflightChecksUtils(rskContext));

        when(preflightChecksUtilsSpy.getJavaVersion()).thenReturn("16");

        Exception exception = Assertions.assertThrows(PreflightCheckException.class, preflightChecksUtilsSpy::runChecks);
        Assertions.assertEquals("Invalid Java Version '16'. Supported versions: 8 11 17", exception.getMessage());

        rskContext.close();
    }

    @Test
    public void runChecks_currentJavaVersionIs17_OK() throws Exception {
        RskContext rskContext = new RskTestContext(new String[0]);
        PreflightChecksUtils preflightChecksUtilsSpy = spy(new PreflightChecksUtils(rskContext));

        when(preflightChecksUtilsSpy.getJavaVersion()).thenReturn("17");

        preflightChecksUtilsSpy.runChecks();

        verify(preflightChecksUtilsSpy, times(1)).getJavaVersion();
        verify(preflightChecksUtilsSpy, times(1)).getIntJavaVersion("17");

        rskContext.close();
    }

    @Test
    public void runChecks_currentJavaVersionIs11_OK() throws Exception {
        RskContext rskContext = new RskTestContext(new String[0]);
        PreflightChecksUtils preflightChecksUtilsSpy = spy(new PreflightChecksUtils(rskContext));

        when(preflightChecksUtilsSpy.getJavaVersion()).thenReturn("11");

        preflightChecksUtilsSpy.runChecks();

        verify(preflightChecksUtilsSpy, times(1)).getJavaVersion();
        verify(preflightChecksUtilsSpy, times(1)).getIntJavaVersion("11");

        rskContext.close();
    }

    @Test
    public void runChecks_runAllChecks_OK() throws Exception {
        RskContext rskContext = new RskTestContext(new String[0]);
        PreflightChecksUtils preflightChecksUtilsSpy = spy(new PreflightChecksUtils(rskContext));

        when(preflightChecksUtilsSpy.getJavaVersion()).thenReturn("17.0.3");

        preflightChecksUtilsSpy.runChecks();

        verify(preflightChecksUtilsSpy, times(1)).getJavaVersion();
        verify(preflightChecksUtilsSpy, times(1)).getIntJavaVersion("17.0.3");
        verify(preflightChecksUtilsSpy, times(1)).checkSupportedJavaVersion();

        rskContext.close();
    }

}
