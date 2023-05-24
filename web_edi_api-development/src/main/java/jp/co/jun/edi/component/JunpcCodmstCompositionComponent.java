package jp.co.jun.edi.component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jp.co.jun.edi.model.CompositionModel;
import jp.co.jun.edi.repository.MCodmstRepository;
import jp.co.jun.edi.type.MCodmstTblIdType;

/**
 * 組成マスタ関連のコンポーネント.
 */
@Component
public class JunpcCodmstCompositionComponent extends GenericComponent {

    @Autowired
    private MCodmstRepository mCodmstRepository;

    /**
     * 入力された組成コードリストが全て組成マスタに存在するかチェックする.
     *
     * @param compositionList 組成リスト
     * @return true : 入力された組成コードが全て組成マスタに存在する, false : 組成マスタに存在しない組成コードがある
     */
    public boolean isAllExitsCompositionCodeList(final List<CompositionModel> compositionList) {
        List<String> compositionCodeList = new ArrayList<>();
        for (CompositionModel composition : compositionList) {
            String code = composition.getCompositionCode();
            String name = composition.getCompositionName();

            if (StringUtils.isEmpty(code)) {
                if (StringUtils.isNotEmpty(name)) {
                    // 名称はあるがコードがない場合、エラー
                    return false; // 処理終了
                }
            } else {
                // codeが入力されていればリストに格納
                compositionCodeList.add(code);
            }
        }

        // 重複除去
        List<String> uniqueCompositionCodeList = new ArrayList<String>(new HashSet<>(compositionCodeList));

        // 入力が0件であればチェックしない
        int uniqueCompositionCodeCnt = uniqueCompositionCodeList.size();
        if (uniqueCompositionCodeCnt == 0) {
            return true;
        }

        // コードリストで抽出した件数取得
        int recordCnt = mCodmstRepository.countByTblidAndCode1List(
                MCodmstTblIdType.COMPOSITION.getValue(),
                uniqueCompositionCodeList);

        return recordCnt == uniqueCompositionCodeCnt;
    }
}
