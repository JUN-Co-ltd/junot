package jp.co.jun.edi.component.model.adapter;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import jp.co.jun.edi.type.SpecialtyQubeCancelStatusType;

/**
 * 店別配分キャンセルAPIレスポンスステータス区分の型変換.
 */
public class SpecialtyQubeCancelStatusTypeAdapter extends XmlAdapter<Integer, SpecialtyQubeCancelStatusType> {

    @Override
    public Integer marshal(final SpecialtyQubeCancelStatusType attribute) {
        return SpecialtyQubeCancelStatusType.convertToValue(attribute);
    }

    @Override
    public SpecialtyQubeCancelStatusType unmarshal(final Integer dbData) {
        return SpecialtyQubeCancelStatusType.convertToType(dbData);
    }

}
