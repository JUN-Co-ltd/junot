package jp.co.jun.edi.entity.extended;

import java.math.BigInteger;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jp.co.jun.edi.entity.GenericEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 拡張優良誤認検査ファイル情報のEntity.
 */
@Entity
@Table(name = "t_misleading_representation_file")
@EntityListeners(AuditingEntityListener.class)
@Data
@EqualsAndHashCode(callSuper = true)
public class ExtendedTMisleadingRepresentationFileEntity extends GenericEntity {

    private static final long serialVersionUID = 1L;
    /** ID. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private BigInteger id;

    /** ファイルID. */
    @Column(name = "file_no_id")
    private BigInteger fileNoId;

    /** 品番ID. */
    @Column(name = "part_no_id")
    private BigInteger partNoId;

    /** コンテンツタイプ. */
    @Column(name = "content_type")
    private String contentType;

    /** ファイル名. */
    @Column(name = "file_name")
    private String fileName;

    /** メモ. */
    @Column(name = "memo")
    private String memo;
}
