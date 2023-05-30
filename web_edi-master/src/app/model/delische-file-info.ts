import { DelischeFileStatus } from '../const/const';

/**
 * デリスケファイル情報を保持するModel.
 */
export class DelischeFileInfo {
    /** ID. */
    id: number;
    /** ファイルID. */
    fileNoId: number;
    /** ステータス. */
    status: number;
    /** 作成日時. */
    createdAt: string;
}
