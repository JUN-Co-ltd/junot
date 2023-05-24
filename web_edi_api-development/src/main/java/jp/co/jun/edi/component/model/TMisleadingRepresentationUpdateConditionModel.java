package jp.co.jun.edi.component.model;

import java.math.BigInteger;

import lombok.Data;

/**
 * 優良誤認承認更新用項目Model.
 */
@Data
public class TMisleadingRepresentationUpdateConditionModel {
    private BigInteger updatedUserId = null;
    private BigInteger partNoId = null;
    private ItemChangeStateModel itemChangeState = null;
}
