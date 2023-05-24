package jp.co.jun.edi.component;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jp.co.jun.edi.model.CompositionModel;
import jp.co.jun.edi.repository.MItemPartsRepository;

/**
 * パーツマスタ関連のコンポーネント.
 */
@Component
public class ItemPartsComponent extends GenericComponent {

    @Autowired
    private MItemPartsRepository mItemPartsRepository;

    /**
     * 入力されたパーツIDリストが全て組成マスタに存在するかチェックする.
     *
     * @param compositionList 組成リスト
     * @return true : パーツマスタに入力されたパーツIDが全て存在する, false : パーツマスタに入力されたパーツIDが存在しないレコードがある
     */
    public boolean isAllExitsItemPartsIdList(final List<CompositionModel> compositionList) {
        List<BigInteger> partsIdList = new ArrayList<>();
        for (CompositionModel composition : compositionList) {
            String id = composition.getPartsCode();
            String name = composition.getPartsName();
            if (StringUtils.isEmpty(id)) {
                if (StringUtils.isNotEmpty(name) && StringUtils.isEmpty(id)) {
                    // 名称はあるがidがない場合、エラー
                    return false; // 処理終了
                }
            } else {
                if (!NumberUtils.isDigits(id)) {
                    // idが数値でなければエラー
                    return false; // 処理終了
                }
                // 数値のidが入力されていればリストに格納
                partsIdList.add(new BigInteger(id));
            }
        }

        // 重複除去
        List<BigInteger> uniquePartsIdList = new ArrayList<BigInteger>(new HashSet<>(partsIdList));

        // 入力が0件であればチェックしない
        int uniquePartsIdCnt = uniquePartsIdList.size();
        if (uniquePartsIdCnt == 0) {
            return true;
        }

        // IDリストで抽出した件数取得
        int recordCnt = mItemPartsRepository.countByIdList(uniquePartsIdList);

        return recordCnt == uniquePartsIdCnt;
    }
}
