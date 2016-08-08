package com.ctrip.xpipe.redis.console.ds;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.springframework.util.ClassUtils;
import org.unidal.dal.jdbc.datasource.DataSourceProvider;
import org.unidal.dal.jdbc.datasource.DefaultDataSourceProvider;
import org.unidal.dal.jdbc.datasource.model.entity.DataSourcesDef;
import org.unidal.lookup.annotation.Named;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
@Named(type = DataSourceProvider.class, value = "xpipe")
public class XpipeDataSourceProvider implements DataSourceProvider, LogEnabled, Initializable {
    private Logger m_logger;
    private String m_datasourceFile;
    private String m_baseDirRef;
    private String m_defaultBaseDir;
    private DataSourceProvider m_delegate;

    private static final String apolloDataSourceProviderClass =
            "com.ctrip.framework.apollo.ds.ApolloDataSourceProvider";
    private static boolean apolloDataSourceProviderPresent =
            ClassUtils.isPresent(apolloDataSourceProviderClass, XpipeDataSourceProvider.class.getClassLoader());

    @Override
    public void initialize() throws InitializationException {
        if (apolloDataSourceProviderPresent) {
            try {
                m_delegate = (DataSourceProvider)(Class.forName(apolloDataSourceProviderClass).newInstance());
            } catch (Throwable ex) {
                m_logger.error("Loading apollo datasource provider failed", ex);
            }
        }
        if (m_delegate == null) {
            m_delegate = createDefaultDataSourceProvider();
        }
        if (m_delegate instanceof LogEnabled) {
            ((LogEnabled)m_delegate).enableLogging(m_logger);
        }
    }

    private DefaultDataSourceProvider createDefaultDataSourceProvider() {
        DefaultDataSourceProvider ds = new DefaultDataSourceProvider();
        ds.setBaseDirRef(m_baseDirRef);
        ds.setDatasourceFile(m_datasourceFile);
        ds.setDefaultBaseDir(m_defaultBaseDir);
        return ds;
    }

    @Override
    public void enableLogging(Logger logger) {
        m_logger = logger;
    }

    @Override
    public DataSourcesDef defineDatasources() {
        return m_delegate.defineDatasources();
    }

    public void setBaseDirRef(String baseDirRef) {
        this.m_baseDirRef = baseDirRef;
    }

    public void setDatasourceFile(String datasourceFile) {
        this.m_datasourceFile = datasourceFile;
    }

    public void setDefaultBaseDir(String defaultBaseDir) {
        this.m_defaultBaseDir = defaultBaseDir;
    }
}
