import { Component, OnInit, OnDestroy } from '@angular/core';
import { FormGroup, FormBuilder } from '@angular/forms';
import { TranslateService } from '@ngx-translate/core';

import { Subscription, Observable, of } from 'rxjs';
import { tap, flatMap, catchError, finalize } from 'rxjs/operators';

import { Const } from '../../const/const';

import { FileUtils } from '../../util/file-utils';

import { HeaderService } from '../../service/header.service';
import { LoadingService } from '../../service/loading.service';
import { BulkRegistItemService } from '../../service/bulk-regist-item.service';
import { MessageConfirmModalService } from '../../service/message-confirm-modal.service';

import { ItemRegistType } from '../../enum/item-regist-type.enum';

import { TotalCount } from './interface/total-count';

import { BulkRegistItemResult } from '../../model/bulk-regist-item-result';
import { BulkRegistItem } from '../../model/bulk-regist-item';

@Component({
  selector: 'app-bulk-regist-item',
  templateUrl: './bulk-regist-item.component.html',
  styleUrls: ['./bulk-regist-item.component.scss']
})
export class BulkRegistItemComponent implements OnInit, OnDestroy {

  /** 登録種別enumの定義 */
  readonly REGIST_TYPE: typeof ItemRegistType = ItemRegistType;

  /** フォーム */
  mainForm: FormGroup;

  /** Submit処理のメッセージタイトル */
  private messageTitle: string;

  /** 画面に表示するメッセージ */
  message = {
    /** フッター */
    footer: {
      /** 正常系 */
      success: { code: '', param: null },
      /** 異常系 */
      error: { code: '', param: null }
    }
  };

  /** ローディング中 */
  isLoading = true;

  /** 未チェックフラグ */
  noChecked = true;

  /** 一括登録添付ファイル */
  bulkRegistFile: File = null;

  /** チェック結果リスト */
  checkResults: BulkRegistItemResult[] = [];

  /** チェック結果の合計数 */
  total: TotalCount = { item: 0, itemError: 0, sku: 0, skuError: 0 };

  /** チェックエラーメッセージリスト */
  checkErrorMessages: string[] = [];

  /** 結果テーブル表示フラグ */
  isShowTable = false;

  /** ローディング用のサブスクリプション */
  private loadingSubscription: Subscription;

  constructor(
    private headerService: HeaderService,
    private formBuilder: FormBuilder,
    private loadingService: LoadingService,
    private bulkRegistItemService: BulkRegistItemService,
    private messageConfirmModalService: MessageConfirmModalService,
    private translateService: TranslateService
  ) { }

  ngOnInit() {
    this.headerService.show();
    this.loadingService.clear();
    this.loadingSubscription = this.loadingService.isLoading$.subscribe((isLoading) => this.isLoading = isLoading);
    this.setInitialTranslate().subscribe();
    this.createFormGroup();
  }

  ngOnDestroy(): void {
    this.loadingSubscription.unsubscribe();
  }

  /**
   * フォームを作成する.
   */
  private createFormGroup(): void {
    this.mainForm = this.formBuilder.group({
      registType: [ItemRegistType.PART_NO]  // 登録種別
    });

    // ラジオボタン変更時、メッセージとチェック結果を初期化
    this.mainForm.controls.registType.valueChanges.subscribe(() => {
      this.clearMessage();
      this.clearCheckResult();
    });
  }

  /**
   * 画面表示に必要な翻訳テキストを設定する.
   * @returns Observable<void>
   */
  private setInitialTranslate(): Observable<void> {
    return this.translateService.get('TITLE.ITEM')
      .pipe(
        tap(title => this.messageTitle = title)
      );
  }

  /**
   * ファイル選択時の処理.
   * @param selectedFiles 選択されたファイルリスト
   */
  onFileSelect(selectedFiles: FileList): void {
    this.onFileDelete();
    if (this.isSelectedFilesError(selectedFiles)) { return; }
    this.bulkRegistFile = Array.from(selectedFiles)[0];
  }

  /**
   * 添付ファイルリストのエラーチェック.
   * @param selectedFiles 添付ファイルリスト
   * @returns true:エラーあり
   */
  private isSelectedFilesError(selectedFiles: FileList): boolean {
    const selectedFile = Array.from(selectedFiles)[0];
    return this.isFilesLengthOver(selectedFiles)
      || this.isFileSizeOver(selectedFile)
      || this.isCorrectFileExtension(selectedFile);
  }

  /**
   * @param selectedFiles 添付ファイルリスト
   * @returns true:ファイル添付数の上限超えエラー
   */
  private isFilesLengthOver(selectedFiles: FileList) {
    if (Const.MAX_FILES_ONE >= selectedFiles.length) {
      return false;
    }

    // ファイルの添付可能数を超えている場合、エラーメッセージを表示
    this.message.footer.error.code = 'ERRORS.FILE_LIMIT_COUNT_ERROR';
    this.message.footer.error.param = Const.MAX_FILES_ONE.toString();
    return true;
  }

  /**
   * @param selectedFile 添付ファイル
   * @returns true:ファイルサイズの上限超えエラー
   */
  private isFileSizeOver(selectedFile: File): boolean {
    if (Const.MAX_FILE_SIZE > selectedFile.size) {
      return false;
    }

    // ファイルサイズの上限を超えた場合、エラーメッセージを表示
    this.message.footer.error.code = 'ERRORS.FILE_SIZE_OVER_ERROR';
    this.message.footer.error.param = Const.MAX_FILE_SIZE_VIEW;
    return true;
  }

  /**
   * @param selectedFile 添付ファイル
   * @returns true:拡張子不正エラー
   */
  private isCorrectFileExtension(selectedFile: File): boolean {
    if (selectedFile.name.match(/\.(xls|xlsx)$/i)) {
      return false;
    }

    // 指定の拡張子以外が選択された場合、エラーメッセージを表示
    this.message.footer.error.code = 'ERRORS.FILE_UNMATCH_EXTENSION';
    return true;
  }

  /**
   * 一括登録ファイルリンク押下時の処理.
   */
  onFileLink(): void {
    FileUtils.downloadFile(this.bulkRegistFile, this.bulkRegistFile.name);
  }

  /**
   * 一括登録ファイル削除処理.
   */
  onFileDelete(): void {
    this.bulkRegistFile = null;
    this.clearCheckResult();
    this.clearMessage();
  }

  /**
   * チェックボタン押下時の処理.
   */
  onCheck(): void {
    const httpFn = this.mainForm.value.registType === ItemRegistType.PART_NO
      ? this.bulkRegistItemService.registValidate() : this.bulkRegistItemService.preRegistValidate();
    this.loading(httpFn, this.setCheckResult());
  }

  /**
   * 登録ボタン押下時の処理.
   */
  onEntry(): void {
    const httpFn = this.mainForm.value.registType === ItemRegistType.PART_NO
      ? this.bulkRegistItemService.regist() : this.bulkRegistItemService.preRegist();
    this.loading(httpFn, this.setSubmitMessage());
  }

  /**
   * チェック結果を設定する.
   * @param res チェック結果レスポンス
   */
  private setCheckResult = () => (res: BulkRegistItem): void => {
    this.checkErrorMessages = res.errors;
    const results = res.results;
    this.checkResults = results;
    this.total = results.reduce(this.totalMapper, { item: 0, itemError: 0, sku: 0, skuError: 0 });
    this.isShowTable = true;
  }

  /**
   * @param acc TotalCountアキュムレータ
   * @param cur 処理中のBulkRegistItemResult
   * @returns total
   */
  private totalMapper = (acc: TotalCount, cur: BulkRegistItemResult): TotalCount => ({
    item: acc.item += cur.itemCount,
    itemError: acc.itemError += cur.errorItemCount,
    sku: acc.sku += cur.skuCount,
    skuError: acc.skuError += cur.errorSkuCount
  })

  /**
   * Submit完了メッセージを設定する.
   */
  private setSubmitMessage = () => (): void => {
    this.message.footer.success = {
      code: 'SUCSESS.BULK_REGIST_ENTRY',
      param: { value: this.messageTitle }
    };
  }

  /**
   * ローディング処理.
   * @param httpFn http通信関数
   * @param afterHttpFn httpレスポンス処理関数
   */
  private loading(httpFn: (bulkRegistFile: File) => Observable<BulkRegistItem>, afterHttpFn: (res: any) => void) {
    let loadingToken = null;
    this.loadingService.start().pipe(
      tap(token => loadingToken = token),
      tap(this.clearMessage),
      flatMap(() => httpFn(this.bulkRegistFile)),
      tap(afterHttpFn),
      catchError(error => this.messageConfirmModalService.openErrorModal(error)),
      finalize(() => this.loadingService.stop(loadingToken))
    ).subscribe();
  }

  /**
   * @returns true:チェックボタン非活性
   */
  isCheckBtnDisable(): boolean {
    return this.bulkRegistFile == null || this.isLoading;
  }

  /**
   * @returns true:Submitボタン非活性
   */
  isSubmitBtnDisable(): boolean {
    return this.total.item === 0 || this.isLoading;
  }

  /**
   * メッセージをクリアする.
   */
  private clearMessage(): void {
    this.message = {
      footer: {
        success: { code: '', param: null },
        error: { code: '', param: null }
      }
    };
  }

  /**
   * チェック結果をクリアする.
   */
  private clearCheckResult(): void {
    this.isShowTable = false;
    this.noChecked = true;
    this.checkResults = [];
    this.checkErrorMessages = [];
    this.total = { item: 0, itemError: 0, sku: 0, skuError: 0 };
  }
}
