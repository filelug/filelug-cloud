package org.clopuccino.domain;

/**
 * <code>MoveOrCopyBean</code> is used for moving or coping a directory or a file.
 *
 * @author masonhsieh
 * @version 1.0
 */
public class MoveOrCopyBean {

    private String source;

    private String target;

    public MoveOrCopyBean() {}

    public MoveOrCopyBean(String source, String target) {
        this.source = source;
        this.target = target;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("MoveOrCopyBean{");
        sb.append("source='").append(source).append('\'');
        sb.append(", target='").append(target).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
