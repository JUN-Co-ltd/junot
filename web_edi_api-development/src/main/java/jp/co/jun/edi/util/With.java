package jp.co.jun.edi.util;

import java.util.function.Consumer;
import java.util.function.ObjIntConsumer;

/**
 * Streamのラムダ式拡張用クラス.
 */
public final class With {
    /**
     * コンストラクタ.
     */
    private With() {
    }

    /**
     * Stream.forEach()拡張用. インデックス付きのforEach()を使いたい場合に利用する.
     *
     * <pre>
     *   java.util.stream.Stream.of("a", "b", "c", "d").forEach(With.index(1, (value, index) -> System.out.println(index + ": " + value)));
     *     1: a
     *     2: b
     *     3: c
     *     4: d
     * </pre>
     *
     * @param <T>
     *            ジェネリクス
     * @param start
     *            index開始番号（初期値）
     * @param consumer
     *            関数型インターフェース
     * @return インデックス付の引数を返す
     */
    public static <T> Consumer<T> index(final int start, final ObjIntConsumer<T> consumer) {
        int[] counter = {start};
        return obj -> consumer.accept(obj, counter[0]++);
    }
}
