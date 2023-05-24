package jp.co.jun.edi.entity.extended;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jp.co.jun.edi.entity.converter.FukukitaruMasterMaterialTypeConverter;
import jp.co.jun.edi.entity.extended.key.ExtendedMFInputAssistSetKey;
import jp.co.jun.edi.type.FukukitaruMasterMaterialType;
import lombok.Data;

/**
 * 拡張入力補助セット情報のEntity.
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
@Data
public class ExtendedMFInputAssistSetEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 複合主キー. */
    @EmbeddedId
    private ExtendedMFInputAssistSetKey id;

    /** セット名. */
    @Column(name = "set_name")
    private String setName;

    /** 資材種別. */
    @Convert(converter = FukukitaruMasterMaterialTypeConverter.class)
    @Column(name = "material_type")
    private FukukitaruMasterMaterialType materialType;

    /** フクキタル用資材情報IDリスト. */
    @Column(name = "material_id_list")
    private String materialIdList;

}
