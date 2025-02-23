package ivr.alarmregions.authentication;

import com.genesyslab.platform.applicationblocks.com.ConfServiceFactory;
import com.genesyslab.platform.applicationblocks.com.IConfService;
import com.genesyslab.platform.commons.protocol.Endpoint;
import com.genesyslab.platform.configuration.protocol.ConfServerProtocol;
import com.genesyslab.platform.configuration.protocol.types.CfgAppType;
import com.genesyslab.platform.standby.WarmStandby;
import com.genesyslab.platform.standby.exceptions.WSException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GenesysConfiguration {

    @Value("${genesys.cfg.server}")
    private String cfgServer;

    @Value("${genesys.cfg.port}")
    private int cfgPort;

    @Value("${genesys.cfg.appName}")
    private String appName;

    @Value("${genesys.cfg.backup.server}")
    private String cfgBackupServer;

    @Value("${genesys.cfg.backup.port}")
    private int cfgBackupPort;

    @Bean
    public IConfService confServerProtocol() {
        ConfServerProtocol confServerProtocol = new ConfServerProtocol();
        confServerProtocol.setClientApplicationType(CfgAppType.CFGThirdPartyServer.asInteger());
        confServerProtocol.setClientName(appName);
        IConfService confService = ConfServiceFactory.createConfService(confServerProtocol);
        WarmStandby warmStandby = new WarmStandby(confServerProtocol, new Endpoint(cfgServer, cfgPort), new Endpoint(cfgBackupServer, cfgBackupPort));
        warmStandby.autoRestore(true);
        try {
            warmStandby.open();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (WSException e) {
            throw new RuntimeException(e);
        }
        return confService;
    }
}
