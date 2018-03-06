package org.clopuccino.domain;

import java.util.Map;

/**
 * <code>HierarchicalFactory</code> is the factory to create classes extends {@code HierarchicalModel}.
 *
 * @author masonhsieh
 * @version 1.0
 */
public class HierarchicalFactory {

    /**
     * Creates {@code HierarchicalModel} instance from the map object. The map object contains the following key and value type:
     * <ol>
     * <li>type: {@link String}, mandatory</li>
     * <li>symlink: {@link String}, mandatory</li>
     * <li>parent: {@link String}, mandatory</li>
     * <li>name: {@link String}, mandatory</li>
     * <li>realParent: {@link String}, mandatory</li>
     * <li>realName: {@link String}, mandatory</li>
     * <li>executable: {@link Boolean}, optional</li>
     * <li>writable: {@link Boolean}, optional</li>
     * <li>readable: {@link Boolean}, optional</li>
     * <li>displaySize: {@link String}, optional</li>
     * <li>sizeInBytes: {@link Long}, optional</li>
     * <li>lastModified: {@link String}, optional. Must be formatted as: <pre>yyyy/MM/dd HH:mm:ss 'GMT'Z</pre></li>
     * </ol>
     *
     * @param map Used to create {@code HierarchicalModel} instance. See above for more details.
     * @return The newly created {@code HierarchicalModel} instance.
     */
    public static HierarchicalModel createHierarchical(Map map) throws IllegalArgumentException {
        Boolean symlink = (Boolean) map.get("symlink");

        if (symlink == null) {
            throw new IllegalArgumentException("Empty value for property 'symlink'");
        }

        String typeString = (String) map.get("type");

        HierarchicalModelType type = HierarchicalModelType.fromString(typeString);
        if (type == null) {
            throw new IllegalArgumentException("Illegal type value: " + typeString);
        }

        String parent = (String) map.get("parent");

        if (parent == null || parent.trim().length() < 1) {
            throw new IllegalArgumentException("Empty value for property 'parent'");
        }

        String name = (String) map.get("name");

        if (name == null || name.trim().length() < 1) {
            throw new IllegalArgumentException("Empty value for property 'name'");
        }

        String realParent = (String) map.get("realParent");

        if (realParent == null || realParent.trim().length() < 1) {
            throw new IllegalArgumentException("Empty value for property 'realParent'");
        }

        String realName = (String) map.get("realName");

        if (realName == null || realName.trim().length() < 1) {
            throw new IllegalArgumentException("Empty value for property 'realName'");
        }

        HierarchicalModel model = new HierarchicalModel();
        model.setSymlink(symlink);
        model.setType(type);
        model.setParent(parent);
        model.setName(name);
        model.setRealParent(realParent);
        model.setRealName(realName);

        Boolean executable = (Boolean) map.get("executable");
        if (executable != null) {
            model.setExecutable(executable);
        }

        Boolean writable = (Boolean) map.get("writable");
        if (writable != null) {
            model.setWritable(writable);
        }

        Boolean readable = (Boolean) map.get("readable");
        if (readable != null) {
            model.setReadable(readable);
        }

        String displaySize = (String) map.get("displaySize");
        if (displaySize != null) {
            model.setDisplaySize(displaySize);
        }

        Long sizeInBytes = (Long) map.get("sizeInBytes");
        if (sizeInBytes != null) {
            model.setSizeInBytes(sizeInBytes);
        }

        Boolean hidden = (Boolean) map.get("hidden");
        if (hidden != null) {
            model.setHidden(hidden);
        }

        String lastModified = (String) map.get("lastModified");
        if (lastModified != null) {
            model.setLastModified(lastModified);
        }

        String contentType = (String) map.get("contentType");
        if (contentType != null) {
            model.setContentType(contentType);
        }

        return model;
    } // end createInstance(Map)

}
