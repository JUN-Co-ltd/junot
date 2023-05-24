package jp.co.jun.edi.entity;

import java.math.BigInteger;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * The persistent class for the t_f_linking_file database table.
 *
 */
@Entity
@Table(name = "t_f_linking_file")
@EntityListeners(AuditingEntityListener.class)
@Data
@EqualsAndHashCode(callSuper = true)
public class TFLinkingFileEntity extends GenericEntity {
    private static final long serialVersionUID = 1L;

    /** ID. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private BigInteger id;

    /** フクキタル発注ID. */
    @Column(name = "f_order_id")
    private BigInteger fOrderId;

    /** コンテンツタイプ. */
    @Column(name = "content_type")
    private String contentType;

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

}
