package jp.co.jun.edi.model;

/**
 * 検索条件のインタフェース.
 */
public interface SearchCondition {
    /**
     * 1つの結果ページで返されるリストの最大数を返却する.
     * @return リストの最大数
     */
    int getMaxResults();

    /**
     * 1つの結果ページで返されるリストの最大数を設定する.
     * @param maxResults リストの最大数
     */
    void setMaxResults(int maxResults);

    /**
     * 戻す結果ページを指定するトークンを返却する.このパラメーターが指定された場合、他のパラメーターは無視する.
     * @return トークン
     */
    String getPageToken();

    /**
     * 戻す結果ページを指定するトークンを設定する.このパラメーターが指定された場合、他のパラメーターは無視する.
     * @param pageToken トークン
     */
    void setPageToken(String pageToken);

    /**
     * 取得対象の結果ページを返却する.0から開始.
     * @return ページ
     */
    int getPage();

    /**
     * 取得対象の結果ページを設定する.0から開始.
     * @param page ページ
     */
    void setPage(int page);
}
