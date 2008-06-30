package brix.demo.util;

import java.io.FileInputStream;
import java.io.InputStream;

import javax.jcr.Repository;

import org.apache.jackrabbit.core.RepositoryImpl;
import org.apache.jackrabbit.core.config.RepositoryConfig;
import org.apache.jackrabbit.rmi.client.ClientRepositoryFactory;
import org.apache.jackrabbit.rmi.jackrabbit.JackrabbitClientAdapterFactory;
import org.apache.wicket.util.file.File;

import brix.jcr.JackrabbitWorkspaceManager;
import brix.jcr.JcrSessionFactory;
import brix.workspace.WorkspaceManager;
import brix.workspace.rmi.ClientWorkspaceManager;

/**
 * Jcr and Jackrabbit related utilities
 * 
 * @author igor.vaynberg
 */
public class JcrUtils
{
    /**
     * Constructor
     */
    private JcrUtils()
    {

    }


    /**
     * Create a {@link WorkspaceManager} implementation. If <code>url</code> starts with
     * <code>rmi://</code> an rmi based workspace manager will be created and returned. If
     * <code>url</code> is left blank, a local workspace manager will be created.
     * 
     * @param url
     * @param brix
     * @return
     */
    public static WorkspaceManager createWorkspaceManager(String url,
            final JcrSessionFactory sessionFactory)
    {
        if (url == null || url.trim().length() == 0)
        {
            // create workspace manager for a file system repository
            return new JackrabbitWorkspaceManager(sessionFactory).initialize();
        }
        else if (url.startsWith("rmi://"))
        {
            // create rmi workspace manager
            return new ClientWorkspaceManager(url);
        }
        else
        {
            throw new RuntimeException("Unsupported workspace manager url: " + url);
        }
    }

    /**
     * Creates a jackrabbit repository based on the url. Accepted urls are <code>rmi://</code> and
     * <code>file://</code>
     * 
     * @param url
     *            repository url
     * @throws RuntimeException
     *             if repository could not be created
     * @return repository instance
     */
    public static Repository createRepository(String url)
    {
        if (url.startsWith("rmi://"))
        {
            return createRmiRepository(url);
        }
        else if (url.startsWith("file://"))
        {
            return createFileRepository(url);
        }
        else
        {
            throw new RuntimeException(
                "Unsupported repository location url. Only prefix rmi:// and file:// are supported");
        }
    }

    /**
     * Creates a repository at the location specified by the url. Url must start with
     * <code>file://</code>.
     * 
     * @param url
     *            repository home url
     * @throws RuntimeException
     *             if repository could not be created
     * @return repository instance
     */
    public static Repository createFileRepository(String url)
    {
        try
        {
            // ensure home dir exists
            final File home = new File(url.substring(6));
            FileUtils.mkdirs(home);

            // create default config file if one is not present
            File cfg = new File(home, "repository.xml");
            if (!cfg.exists())
            {
                FileUtils.copyClassResourceToFile("/brix/demo/repository.xml", cfg);
            }

            InputStream configStream = new FileInputStream(cfg);
            RepositoryConfig config = RepositoryConfig.create(configStream, home.getAbsolutePath());
            return RepositoryImpl.create(config);
        }
        catch (Exception e)
        {
            throw new RuntimeException("Could not create file repository at url: " + url, e);
        }
    }

    /**
     * Creates a repository at the location specified by the url. Url must start with
     * <code>rmi://</code>.
     * 
     * @param url
     *            repository home url
     * @throws RuntimeException
     *             if repository could not be created
     * @return repository instance
     */
    public static Repository createRmiRepository(String url)
    {
        try
        {
            ClientRepositoryFactory factory = new ClientRepositoryFactory(
                new JackrabbitClientAdapterFactory());
            Repository repository = factory.getRepository(url);
            return repository;
        }
        catch (Exception e)
        {
            throw new RuntimeException("Could not create rmi repository instance at url: " + url, e);
        }
    }

}
