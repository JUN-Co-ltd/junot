package jp.co.jun.edi.component;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jp.co.jun.edi.repository.MCodmstRepository;
import jp.co.jun.edi.type.MCodmstTblIdType;

/**
 * 丸井品番マスタ関連のコンポーネント.
 */
@Component
public class JunpcCodmstMaruiItemComponent extends GenericComponent {

    @Autowired
    private MCodmstRepository mCodmstRepository;

    /**
     * 指定された丸井品番が丸井品番マスタに存在するかチェックする.
     *
     * @param brandCode ブランドコード
     * @param maruiGarmentNo 丸井品番
     * @return true : 丸井品番マスタに指定された丸井品番が存在する, false : 丸井品番マスタに指定された丸井品番が存在しない
     */
    public boolean isExitsMaruiGarmentNo(final String brandCode, final String maruiGarmentNo) {
        return mCodmstRepository.countByTblidAndCode1AndCode3(
                MCodmstTblIdType.MARUI_ITEM.getValue(), brandCode, maruiGarmentNo) > 0;
    }

    /**
     * 指定されたブランドに紐づく丸井品番リストがあるかチェックする.
     *
     * @param brandCode ブランドコード
     * @return true : 丸井品番リストがある, false : 丸井品番リストがない
     */
    public boolean isExitsMaruiItemList(final String brandCode) {
        return mCodmstRepository.countMaruiItemByTblidAndCode1(
                MCodmstTblIdType.MARUI_ITEM.getValue(), brandCode) > 0;
    }
}
