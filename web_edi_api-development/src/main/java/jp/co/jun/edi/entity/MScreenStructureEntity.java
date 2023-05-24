package jp.co.jun.edi.entity;

import java.math.BigInteger;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jp.co.jun.edi.entity.converter.BooleanTypeConverter;
import jp.co.jun.edi.entity.converter.MCodmstTblIdTypeConverter;
import jp.co.jun.edi.entity.converter.MScreenStructureConverter;
import jp.co.jun.edi.entity.extended.ExtendedMScreenStructureEntity;
import jp.co.jun.edi.type.BooleanType;
import jp.co.jun.edi.type.MCodmstTblIdType;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 画面構成マスタEntity.
 */
@Entity
@Table(name = "m_screen_structure")
@EntityListeners(AuditingEntityListener.class)
@Data
@EqualsAndHashCode(callSuper = true)
public class MScreenStructureEntity extends GenericDeletedAtUnixEntity {
    private static final long serialVersionUID = 1L;

    /** ID. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private BigInteger id;

    /** マスタコード. */
    @Convert(converter = MCodmstTblIdTypeConverter.class)
    @Column(name = "tblid")
    private MCodmstTblIdType tableId;

    /** マスタ名称. */
    @Column(name = "name")
    private String name;

    /** 画面構成定義（JSON形式データ）. */
    @Convert(converter = MScreenStructureConverter.class)
    @Column(name = "structure")
    private List<ExtendedMScreenStructureEntity> structure;

    /** テーブル名称. */
    @Column(name = "table")
    private String tableName;

    /** UNIX削除日時フラグ. */
    @Convert(converter = BooleanTypeConverter.class)
    @Column(name = "deleted_at_unix_flg")
    private BooleanType deletedAtUnixFlg;

}
