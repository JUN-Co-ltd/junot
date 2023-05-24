// CHECKSTYLE:OFF
package jp.co.jun.edi.repository;

import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static uk.co.datumedge.hamcrest.json.SameJSONAs.*;

import java.io.File;
import java.util.Iterator;
import java.util.Map;

import org.dbunit.Assertion;
import org.dbunit.dataset.ITable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import test.utils.CSVDataSetController;
import test.utils.FileController;

/**
 * Repositoryテスト共通クラス.
 *
 */
public class RepositoryTest {
    @Autowired
    private MockMvc mvc;

   private static final String TEST_DATA_ROOT = "C:/dev/jun/test/";

    /**
     * テストデータのルートフォルダパスとfileRelativePathを結合し、ファイルの絶対パスを返す
     * @param fileRelativePath 相対パス
     * @return
     */
    protected String getFileAbsolutePath(final String fileRelativePath) throws Exception {
        String path = fileRelativePath;
        if (fileRelativePath.substring(0, 1).equals("/")) {
            //fileRelativePathの先頭が「/」で始まる場合は、先頭の「/」を削除する
            path = fileRelativePath.substring(1);
        }
        if (new File(TEST_DATA_ROOT.concat(path)).exists()) {
            //ファイルが存在する場合、絶対パスを返す
            return TEST_DATA_ROOT.concat(path);
        }

        throw new Exception("ファイルが見つかりません[" + TEST_DATA_ROOT.concat(path) + "]");
    }

    /**
     * テスト共通処理.
     */
    protected void execute(String initFilePath, MockHttpServletRequestBuilder requestParam, String responseFile, String actualDbFile) throws Exception {
        CSVDataSetController cSVDataSetController = new CSVDataSetController();
        // テストデータ投入
        cSVDataSetController.setUp(initFilePath);

        // リクエスト送信（POST）
        ResultActions action = mvc.perform(requestParam); // パラメータ

        // リクエスト、レスポンス情報をコンソール出力
        MvcResult result = action
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk()) //HTTPレスポンスコード：200
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))//コンテンツタイプ：JSON形式
                .andReturn();

        // 期待値：レスポンスデータ
        String expected = FileController.readFileSJIS(responseFile);
        assertThat(result.getResponse().getContentAsString(), sameJSONAs(expected));

        // 期待値：DBデータ
        Map<String, ITable> expectedDataSet = cSVDataSetController.getExpected(actualDbFile);
        // 実値：DBデータ
        Map<String, ITable> actualDataSet = cSVDataSetController.getActualData(expectedDataSet);

        Iterator<String> it = expectedDataSet.keySet().iterator();
        while (it.hasNext()) {
            String tableName = it.next();
            Assertion.assertEquals(expectedDataSet.get(tableName), actualDataSet.get(tableName));
        }

    }

}
//CHECKSTYLE:ON
