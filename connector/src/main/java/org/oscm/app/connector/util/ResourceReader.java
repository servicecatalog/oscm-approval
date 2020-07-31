package org.oscm.app.connector.util;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;

import org.apache.log4j.Logger;

/**
 * Class to locate resource files by ClassLoader features. The resource file has
 * to be found within the classpath. This class can be used by calling the
 * static load methods with the fully specified resource path or by creating an
 * instance with a prefix string that is concatenated with the arguments of
 * consecutive calls to the getResource methods.
 *
 * @author opetrovski
 */
public class ResourceReader {
    private static Logger logger = Logger.getLogger(ResourceReader.class);

    // prefix used for all lookup methods
    private String resourcePrefix;

    /**
     * Creates an instance that looks for resource files with the lookup methods
     * appending the specified prefix and the resource name. Example:
     * ResourceReader reader = new ResourceReader("/lib/ini/system/fwk/"); URL
     * url = reader.getResource("enums.xml"); ... The reader instance will then
     * try to locate the resource file /lib/ini/system/fwk/enums.xml within the
     * classpath
     */
    public ResourceReader(String pResourcePrefix) {
        this.resourcePrefix = pResourcePrefix;
    }

    /**
     * Try to locate the resource within the current classpath.
     * 
     * @param resource
     *            the relative path from the prefix spec to the requested
     *            resource.
     * @return the URL of the requested resource file or null if it does not
     *         exist
     */
    public URL getResource(String resource) {
        if (resource == null) {
            throw new IllegalArgumentException(
                    "Resource id must not be <null>");
        }

        try {
            return getClass().getResource(
                    addLeadingSlash(concat(this.resourcePrefix, resource)));
        } catch (Exception ex) {
            logger.error("Exception during resource lookup", ex);
            return null;
        }
    }

    /**
     * Try to locate the resource within the current classpath.
     * 
     * @param resource
     *            the relative path from the prefix spec to the requested
     *            resource.
     * @return an array containing all URL of resource files matching the
     *         requested resource name or an empty array if no resource exists
     *         with this name.
     */
    public URL[] getResources(String resource) {
        if (resource == null) {
            throw new IllegalArgumentException(
                    "Resource id must not be <null>");
        }

        return loadResources(addLeadingSlash(concat(resourcePrefix, resource)));
    }

    /**
     * Try to locate the resource within the current classpath.
     * 
     * @param resource
     *            the relative path from the prefix spec to the requested
     *            resource.
     * @return the InputStream of the requested resource file or null if it does
     *         not exist
     */
    public InputStream getResourceAsStream(String resource) {
        if (resource == null) {
            throw new IllegalArgumentException(
                    "Resource id must not be <null>");
        }

        try {
            return getClass().getResourceAsStream(
                    addLeadingSlash(concat(resourcePrefix, resource)));
        } catch (Exception ex) {
            logger.error("Exception during resource lookup", ex);
            return null;
        }
    }

    /**
     * Concatenate the strings with a separator between the parts. This method
     * takes care of existing separators at the end of the first or at the
     * beginning of the second part. Only one separator will be between the
     * parts in the resulting String. The separator is a slash.
     * 
     * @param prefix
     *            - the first part of the String
     * @param resource
     *            - the second part that is appended to the first part.
     * @return the concatenation of the two parts with exactly one separator
     *         between them.
     *
     */
    public static String concat(String prefix, String resource) {
        StringBuffer buf = new StringBuffer(prefix);
        if (prefix.endsWith("/")) {
            // no more separator allowed
            while (resource.startsWith("/")) {
                resource = resource.substring(1);
            }
            buf.append(resource);
        } else {
            if (!resource.startsWith("/")) {
                buf.append('/');
            }
            buf.append(resource);
        }
        return buf.toString();
    }

    /**
     * Concatenate the resource name with the instance prefix
     */
    private static String addLeadingSlash(String resource) {
        if (!resource.startsWith("/")) {
            StringBuffer buf = new StringBuffer();
            buf.append('/');
            buf.append(resource);
            return buf.toString();
        } else {
            return resource;
        }
    }

    /**
     * Strip a leading slash from the resource path
     */
    private static String stripLeadingSlash(String resource) {
        if (resource.startsWith("/")) {
            return resource.substring(1);
        } else {
            return resource;
        }
    }

    /**
     * Try to locate the resource within the classpath by name as is.
     * 
     * @param resource
     *            the full qualified resource name - must not be null the path
     *            has to start with a slash. Example:
     *            reader.loadResource("/lib/ini/system/fwk/enums.xml");
     * @return the URL of the requested resource file or null if it does not
     *         exist
     */
    public static URL loadResource(String resource) {
        if (resource == null) {
            throw new IllegalArgumentException(
                    "Resource id must not be <null>");
        }

        if (!resource.startsWith("/")) {
            logger.error(
                    "WARNING: ResourceReader - static lookup method resource name should start with /: "
                            + resource);
        }
        try {
            return ResourceReader.class.getResource(addLeadingSlash(resource));
        } catch (Exception ex) {
            logger.error("Exception during resource lookup", ex);
            return null;
        }
    }

    /**
     * Try to locate all resource within the classpath by name. All files with
     * identical names and path are enumerated.
     * 
     * @param resource
     *            the full qualified resource name - must not be null the path
     *            must not start with a slash. Example:
     *            reader.loadResources("lib/ini/system/fwk/enums.xml");
     * @return the URL of the requested resource file or null if it does not
     *         exist
     */
    public static final URL[] loadResources(String resource) {
        if (resource == null) {
            throw new IllegalArgumentException(
                    "Resource id must not be <null>");
        }

        try {
            Enumeration list = ResourceReader.class.getClassLoader()
                    .getResources(stripLeadingSlash(resource));
            if (list.hasMoreElements()) {
                ArrayList arr = new ArrayList();
                while (list.hasMoreElements()) {
                    arr.add(list.nextElement());
                }
                return (URL[]) arr.toArray(new URL[arr.size()]);
            }
        } catch (Exception ex) {
            logger.error("Exception during resource lookup", ex);
        }
        return new URL[0];
    }

    /**
     * Try to locate the resource within the classpath by name as is.
     * 
     * @param resource
     *            the full qualified resource name
     * @return the InputStream of the requested resource file or null if it does
     *         not exist
     */
    public static InputStream loadResourceAsStream(String resource) {
        if (resource == null) {
            throw new IllegalArgumentException(
                    "Resource id must not be <null>");
        }

        if (!resource.startsWith("/")) {
            logger.error(
                    "WARNING: ResourceReader - static lookup method resource name should start with /: "
                            + resource);
        }
        try {
            return ResourceReader.class
                    .getResourceAsStream(addLeadingSlash(resource));
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * Concatenate the resource name with the instance prefix
     */
    public String getCompletePath(String resource) {
        return concat(this.resourcePrefix, resource);
    }

}
