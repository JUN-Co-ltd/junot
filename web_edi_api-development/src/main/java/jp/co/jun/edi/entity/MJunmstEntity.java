package jp.co.jun.edi.entity;

import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jp.co.jun.edi.entity.converter.AllocationTypeConverter;
import jp.co.jun.edi.type.AllocationType;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 発注生産システムの配分順位マスタのEntity.
 */
@Entity
@Table(name = "m_junmst")
@EntityListeners(AuditingEntityListener.class)
@Data
@EqualsAndHashCode(callSuper = true)
public class MJunmstEntity extends GenericEntity {
    private static final long serialVersionUID = 1L;

    /** ID. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;

    /** ブランド. */
    private String brand;

    /** 店舗コード. */
    private String shpcd;

    /** 配分課. */
    private String hka;

    /** 配分順. */
    private Integer hjun;

    /** 配分区分. */
    @Convert(converter = AllocationTypeConverter.class)
    private AllocationType haikbn;

    /** 保留先店舗コード. */
    private String hspcd;

    /** メンテ区分. */
    private String mntflg;

    /** 登録日. */
    private String crtymd;

    /** 修正日. */
    private String updymd;

    /** 入力者. */
    private String tanto;

    /** 送信区分. */
    private String souflg;

    /** 送信日. */
    private String souymd;
}
