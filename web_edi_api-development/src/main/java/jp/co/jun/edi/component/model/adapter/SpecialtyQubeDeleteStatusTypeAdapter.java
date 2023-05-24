package jp.co.jun.edi.component.model.adapter;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import jp.co.jun.edi.type.SpecialtyQubeDeleteStatusType;

/**
 * 全店配分削除APIレスポンスステータス区分の型変換.
 */
public class SpecialtyQubeDeleteStatusTypeAdapter extends XmlAdapter<Integer, SpecialtyQubeDeleteStatusType> {

    @Override
    public Integer marshal(final SpecialtyQubeDeleteStatusType attribute) {
        return SpecialtyQubeDeleteStatusType.convertToValue(attribute);
    }

    @Override
    public SpecialtyQubeDeleteStatusType unmarshal(final Integer dbData) {
        return SpecialtyQubeDeleteStatusType.convertToType(dbData);
    }

}
