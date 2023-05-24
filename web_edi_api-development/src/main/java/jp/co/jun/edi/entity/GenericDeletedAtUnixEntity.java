package jp.co.jun.edi.entity;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

import jp.co.jun.edi.util.DateUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 共通のEntity.
 */
@MappedSuperclass
@Data
@EqualsAndHashCode(callSuper = true)
public abstract class GenericDeletedAtUnixEntity extends GenericEntity {
    private static final long serialVersionUID = 1L;
    /** 削除日時(UNIX時間). */
    @Column(name = "deleted_at_unix")
    private long deletedAtUnix = 0;

    /**
     * 登録時と更新時に実行する処理.
     */
    @PrePersist
    @PreUpdate
    public void prePersistAndUpdate() {
        if (this.getDeletedAt() != null) {
            setDeletedAtUnix(DateUtils.toUnixTimestamp(this.getDeletedAt()));
        }
    }
}
