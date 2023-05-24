package jp.co.jun.edi.entity;

import java.math.BigInteger;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jp.co.jun.edi.entity.converter.BusinessTypeConverter;
import jp.co.jun.edi.entity.converter.WmsLinkingStatusTypeConverter;
import jp.co.jun.edi.type.BusinessType;
import jp.co.jun.edi.type.WmsLinkingStatusType;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 倉庫連携ファイル情報のEntity.
 */
@Entity
@Table(name = "t_wms_linking_file")
@EntityListeners(AuditingEntityListener.class)
@Data
@EqualsAndHashCode(callSuper = true)
public class TWmsLinkingFileEntity extends GenericEntity {
    private static final long serialVersionUID = 1L;

    /** ID. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private BigInteger id;

    /** 業務区分. */
    @Column(name = "business_type")
    @Convert(converter = BusinessTypeConverter.class)
    private BusinessType businessType;

    /** 管理No. */
    @Column(name = "manage_number")
    private String manageNumber;

    /** ファイル名. */
    @Lob
    @Column(name = "file_name")
    private String fileName;

    /** S3のファイルのキー. */
    @Column(name = "s3_key")
    private String s3Key;

    /** S3のプレフィックス. */
    @Column(name = "s3_prefix")
    private String s3Prefix;

    /** ファイル作成日時. */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "file_created_at")
    private Date fileCreatedAt;

    /** WMS連携ステータス. */
    @Column(name = "wms_linking_status")
    @Convert(converter = WmsLinkingStatusTypeConverter.class)
    private WmsLinkingStatusType wmsLinkingStatus;

    /** WMS連携日時. */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "wms_linked_at")
    private Date wmsLinkedAt;
}
