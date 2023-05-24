package jp.co.jun.edi.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.Data;

/**
 * 発注生産システムのサイズマスタのEntity.
 * 検索性能を向上させるため、画面で使用しない項目をコメントアウト化.
 */
@Entity
@Table(name = "m_sizmst")
@EntityListeners(AuditingEntityListener.class)
@Data
public class MSizmstEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;
    private String hscd;
    private String szkg;
    private String jun;
    private String mntflg;
    //    private String souflg;
    //    private String souymd;
    /** 削除日時. */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "deleted_at")
    private Date deletedAt;
}
