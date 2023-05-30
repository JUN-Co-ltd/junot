//PRD_0137 #10669 add start
import { Component, OnInit, OnDestroy } from '@angular/core';
import { MaintSizeService } from 'src/app/service/maint/maint-size.service';
import { MaintCodeResult } from 'src/app/model/maint/maint-code-result';
import { MaintSizeSearch } from 'src/app/model/maint/maint-size-list';
import { MaintTableInfoResult } from 'src/app/model/maint/maint-code-result';
import { MaintCodeSearch } from 'src/app/model/maint/maint-code-result';
import { UpdateCode } from 'src/app/model/maint/maint-code-result';
import { Observable, Subscription } from 'rxjs';
import { FormGroup, FormBuilder, FormControl, Validators, ValidatorFn, FormArray } from '@angular/forms';
import { tap, flatMap, catchError, finalize, filter } from 'rxjs/operators';
import { LoadingService } from 'src/app/service/loading.service';
import { HeaderService } from 'src/app/service/header.service';
import { MessageConfirmModalService } from 'src/app/service/message-confirm-modal.service';
import { NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { TranslateService } from '@ngx-translate/core';
import { MessageConfirmModalComponent } from 'src/app/component/message-confirm-modal/message-confirm-modal.component';
import { FormUtils } from 'src/app/util/form-utils';
import { LocalStorageService } from 'src/app/service/local-storage.service';
import { Session } from 'src/app/model/session';
import { StorageKey } from 'src/app/const/storage-key';
import { SessionService } from 'src/app/service/session.service';
import { MaintSizeSearchCondition } from 'src/app/model/maint/maint-size-search-condition';

/**
 * FormConfigの型宣言.
 */
export class FormConfigs {
  key: string;
  name: string;
  type: string;
  validators: {
    required: boolean;
    minLength: number;
    maxLength: number;
    pattern: string;
  };
}

/**
 * メッセージの型宣言.
 */
export class TranslateEx {
  code: string;
  params: any;
}

@Component({
  selector: 'app-maint-size',
  templateUrl: './maint-size.component.html',
  styleUrls: ['./maint-size.component.scss']
})

export class MaintSizeComponent implements OnInit, OnDestroy {
  /** 画面を非表示にする */
  invisibledMaintForm = true;
  invisibledDataForm = true;
  /** 画面を非活性にする */
  disabled = true;

  deleteButtonDisabled = true;
  updateButtonDisabled = true;

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

  /** ログインユーザーのセッション */
  private session: Session;

  /** 対象マスタテーブル検索用フォーム */
  maintForm: FormGroup;
  /** マスタデータ内検索用フォーム */
  searchForm: FormGroup;
  /** マスタデータ用フォーム */
  dataForm: FormGroup;

  /** マスタテーブル一覧 */
  maintTableList: MaintCodeResult = null;
  /** 指定メンテコード画面構成情報 */
  maintCodeSettings: MaintTableInfoResult;
  /** 指定メンテコード情報リスト */
  maintCode: any;
  /** 更新、削除結果 */
  result: any;

  maintSearchItem: MaintCodeSearch;
  maintSearchCondition: MaintSizeSearchCondition = new MaintSizeSearchCondition;
  /** 項目設定情報 */
  formConfigs = [];
  /** 項目内容情報 */
  formValues = [];
  /** チェックボックスの回帰抑止 */
  checkboxCtrl = false;

  /** 検索項目有効フラグ */
  searchControlTrue = true;

  // 次のページのトークン
  nextPageToken: string;
  // ページ番号
  pageNumber: number;
  // Submit押下
  submitted = false;

  private loadingSubscription: Subscription;

  constructor(
    private maintSizeService: MaintSizeService,
    private messageConfirmModalService: MessageConfirmModalService,
    private headerService: HeaderService,
    private loadingService: LoadingService,
    private modal: NgbModal,
    private formBuilder: FormBuilder,
    private translateService: TranslateService,
    private localStorageService: LocalStorageService,
    private sessionService: SessionService,
  ) { }

  ngOnInit() {
    this.headerService.show();
    this.session = this.sessionService.getSaveSession();
    // ローディングクリア（親画面のみ）
    this.loadingService.clear();

    // ローディングサブスクリプション開始
    this.loadingSubscription = this.loadingService.isLoading$.subscribe(
      (isLoading) => this.disabled = isLoading);

    let loadingToken = null;
    let isError = false;

    // ローディング開始
    this.loadingService.start().pipe(
      // ローディングトークン待避
      tap((token) => loadingToken = token),
      // メッセージをクリア
      tap(() => this.clearMessage()),
      // 画面非表示
      tap(() => {
        this.invisibledMaintForm = true;
        this.invisibledDataForm = true;
      }),
      // 検索用フォームを生成
      tap(() => this.maintForm = this.createMaintFormGroup()),
      // エラーが発生した場合は、エラーモーダルを表示
      catchError((error) => {
        isError = true;
        return this.messageConfirmModalService.openErrorModal(error);
      }),
      finalize(() => {
        // 画面表示（エラーが発生した場合は、非表示のままとする）
        this.invisibledMaintForm = isError;
        // ローディング停止
        this.loadingService.stop(loadingToken);
      })
    ).subscribe();

  }

  ngOnDestroy() {
    // ローディングサブスクリプション停止
    this.loadingSubscription.unsubscribe();
  }

  /**
   * メッセージをクリアする。
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
   * エラー表示の有無を返却する。
   * @param value FormControl
   */
  getErrors(valueGroup: FormGroup): Set<TranslateEx> {//#10671
    const errors = new Set<TranslateEx>();

    Object.keys(valueGroup.controls).forEach(key => {
      // エラー判定
      if (this.isErrorDisplay(<FormControl> valueGroup.controls[key])) {
        Object.keys(valueGroup.controls[key].errors).forEach(errorKey => {
          // 項目情報名の取得
          // const list = this.formConfigs.filter(val => key === val.key);
          if (errorKey === 'required') {
            // バリデーション：必須
            if (key == 'jun') {
              errors.add({ code: 'ERRORS.VALIDATE.ANY_EMPTY', params: { name: '表示順' } } as TranslateEx);
            } else if (key == 'szkg') {
              errors.add({ code: 'ERRORS.VALIDATE.ANY_EMPTY', params: { name: 'サイズ' } } as TranslateEx);
            }

          } else if (errorKey === 'pattern') {
            // バリデーション：正規表現
            if (key == 'jun') {
              errors.add({ code: 'ERRORS.VALIDATE.PATTERN_PROHIBITED_CHAR', params: { name: '表示順' } } as TranslateEx);
            } else if (key == 'szkg') {
              errors.add({ code: 'ERRORS.VALIDATE.PATTERN_PROHIBITED_CHAR', params: { name: 'サイズ' } } as TranslateEx);
            }
          } else if (errorKey === 'minlength') {
            // バリデーション：最小文字数
            if (4 === valueGroup.controls[key].value.length) {
              // 固定文字数エラー
              if (key == 'jun') {
                errors.add({
                  code: 'ERRORS.VALIDATE.LENGTH_NOT_MATCH', params: { name: '表示順は', length: 4 }
                } as TranslateEx);
              }
            } else {
              if (key == 'jun') {
                errors.add({
                  code: 'ERRORS.VALIDATE.LENGTH_NOT_MATCH', params: { name: '表示順は', length: 4 }
                } as TranslateEx);
              }
            }
          } else if (errorKey === 'maxlength') {
            // バリデーション：最大文字数
            if (4 === valueGroup.controls[key].value.length) {
              // 固定文字数エラー
              if (key == 'jun') {
                errors.add({
                  code: 'ERRORS.VALIDATE.LENGTH_NOT_MATCH', params: { name: '表示順は', length: 4 }
                } as TranslateEx);
              } else if (key == 'szkg') {
                errors.add({
                  code: 'ERRORS.VALIDATE.LENGTH_NOT_MATCH', params: { name: 'サイズは', length: 4 }
                } as TranslateEx);
              }
            } else {
              if (key == 'jun') {
                errors.add({
                  code: 'ERRORS.VALIDATE.LENGTH_NOT_MATCH', params: { name: '表示順は', length: 4 }
                } as TranslateEx);
              } else if (key == 'szkg') {
                errors.add({
                  code: 'ERRORS.VALIDATE.LENGTH_EXCEED', params: { name: 'サイズ', length: 4 }
                } as TranslateEx);
              }
            }
          }
        });
      }
    });
    return errors;
  }

  /**
   * dataFormのlistのFormArrayを取得する。
   * @return this.dataForm.get('list') as FormArray
   */
  get dataListFormArray(): FormArray {
    return this.dataForm.get('list') as FormArray;
  }

  /**
   * 選択中のテーブルIDを取得する。
   * @return テーブルID
   */
  get searchTblId(): string {
    return this.maintForm.controls.selectTblId.value;
  }

  /**
 * 入力されたブランドコードを取得する。
 * @return テーブルID
 */
  get brandCD(): string {
    return this.maintForm.controls.brandCode.value;
  }

  /**
 * 選択中のテーブルIDを取得する。
 * @return テーブルID
 */
  get itemCD(): string {
    return this.maintForm.controls.itemCode.value;
  }

  /**
   * エラー表示の有無を返却する。
   * @param value FormControl
   */
  private isErrorDisplay(value: FormControl): boolean {
    return value.invalid && (value.dirty || value.touched || this.submitted);
  }

  /**
   * 対象マスタテーブル検索用FromGroupを作成する.
   * @returns FormGroup
   */
  private createMaintFormGroup(): FormGroup {
    const f = this.formBuilder.group({
      brandCode: '', //ブランドコード
      itemCode: '' //アイテムコード
    });

    f.controls.brandCode.patchValue('');
    f.controls.itemCode.patchValue('');
    return f;
  }

  /**
   * マスタデータのFormGroupを作成する.
   * @returns データ領域のFormGroup
   */
  private createDataFormGroup(): FormGroup {
    // 画面要素追加（チェックボックス）
    this.formValues.forEach(v => v['selected'] = false);

    // データ（行）をループ
    const formArrays = [] as FormGroup[];
    this.formValues.forEach(v => formArrays.push(this.createCodeDataFormGroup(v)));

    return this.formBuilder.group({
      list: this.formBuilder.array(formArrays)
    });

  }

  /**
   * コードデータ領域の項目を包括したFormGroupを作成する。
  * @param rowData 行情報
  * @returns validationを追加したFormGroup
  */
  private createCodeDataFormGroup(rowData): FormGroup {

    const formControls = {} as FormControl[];
    const validatorFn: ValidatorFn[] = [];
    const validatorFnJun: ValidatorFn[] = [];

    // バリデーションを定義する
    // バリデーション：必須
    if (rowData.id !== '') {
      validatorFn.push(Validators.required);
    }
    // バリデーション：最大文字数
    validatorFn.push(Validators.maxLength(4));

    // バリデーション：必須
    if (rowData.id !== '') {
      validatorFnJun.push(Validators.required);
    }

    // バリデーション：最大文字数
    validatorFnJun.push(Validators.maxLength(4));
    // バリデーション：最小文字数
    validatorFnJun.push(Validators.minLength(4));
    // バリデーション：正規表現
    validatorFnJun.push(Validators.pattern(/[0-9]{1,4}$/));


    // FormControl生成（値,{ validators:バリデーション,updateOn:トリガー})
    formControls['id'] = new FormControl(rowData['id']);
    formControls['jun'] = new FormControl(rowData['jun'], { validators: validatorFnJun, updateOn: 'blur' });
    formControls['szkg'] = new FormControl(rowData['szkg'], { validators: validatorFn, updateOn: 'blur' });
    // 構成データ追加（チェックボックス）
    formControls['selected'] = new FormControl(rowData['selected'], { updateOn: 'change' });

    return this.formBuilder.group(formControls);
  }

  /**
   * 編集済みのセルがあるかどうか判定する.
   * @returns 判定結果 true：あり、false：なし
   */
  private isDirty(): boolean {
    let isDirtyFlg = false;
    const list = <FormGroup> this.dataForm.controls['list'];
    Object.keys(list.controls).forEach(key => {
      const formGroup = <FormGroup> list.controls[key];
      Object.keys(formGroup.controls).forEach(val => {
        const formControl = <FormControl> formGroup.controls[val];
        if ((val !== 'selected') && (formControl.dirty === true)) {
          isDirtyFlg = true;
        }
      });
    });
    return isDirtyFlg;
  }

  /**
   * 確認モーダルを表示する.
   * メッセージをセットした確認モーダル表示
   * @param modalTitle モーダルに表示するタイトル
   * @param modalMessage モーダルに表示するメッセージ
   * @param interpolateParams パラメータ
   */
  private openConfirmModal(modalTitle: string, modalMessage: string, interpolateParams?: Object): NgbModalRef {
    const modalRef = this.modal.open(MessageConfirmModalComponent);
    // モーダルのタイトルセット
    this.translateService.get(modalTitle).subscribe(
      (title: string) => modalRef.componentInstance.title = title);
    // モーダルの内容セット
    this.translateService.get(modalMessage, interpolateParams).subscribe((msg: string) =>
      modalRef.componentInstance.message = msg);
    return modalRef;
  }

  /**
   * 検索ボタン押下時の処理.
   *
   * 指定されたテーブル区分のマスタデータを取得する
   * ※編集しているセルがある場合は確認モーダルを表示する
   */
  onSearch(): void {
    // マスタデータ用フォームあり(初回以降) かつ 編集しているセルがある場合、確認モーダル表示
    if (this.dataForm != null && this.isDirty()) {
      const modalRef = this.openConfirmModal('TITLE.CANCEL_EIDT', 'INFO.CANCEL_EIDT_COMFIRM_MESSAGE');
      modalRef.result.then((result: string) => {
        this.referenceMaintTable();
      },
        () => {
          // ×ボタン押下時、処理なし
        });
    } else {
      this.referenceMaintTable();
    }
  }

  /**
 * 指定されたブランドコード、アイテムコードを組み合わせてサイズマスタを参照する.
 */
  private referenceMaintTable(): void {
    let loadingToken = null;
    let isError = false;

    // マスタテーブル情報初期化
    this.initializeMaintTableData();

    // ローディング開始
    this.loadingService.start().pipe(
      // ローディングトークン待避
      tap((token) => loadingToken = token),
      // 画面非表示
      tap(() => this.invisibledDataForm = true),
      // メッセージをクリア
      tap(() => this.clearMessage()),
      // 対象マスタデータ内の検索条件Model作成
      tap(() => this.maintSearchItem = this.createMaintCodeSearchCondition(true)),
      flatMap(() => this.search(this.maintSearchItem)),
      tap(result => this.setSearchResultMessage(result.items)),
      tap(() => this.formValues = this.maintCode.items),
      // フォーム作成
      tap(() => {
        this.dataForm = this.createDataFormGroup();
      }),
      // エラーが発生した場合は、エラーモーダルを表示
      catchError((error) => {
        isError = true;
        return this.messageConfirmModalService.openErrorModal(error);
      }),
      finalize(() => {
        // 画面表示（エラーが発生した場合は、非表示のままとする）
        this.invisibledDataForm = isError;
        // ボタン表示確認
        this.disableFooterButton();
        // ローディング停止
        this.loadingService.stop(loadingToken);
      })
    ).subscribe();
  }

  /**
   * 検索条件をstorageに保持.
   * @param session セッション
   * @param searchCondition 検索条件
   */
  private saveSearchConditions(session: Session): void {
    this.maintSearchCondition.brandCode = this.brandCD;
    this.maintSearchCondition.itemCode = this.itemCD;

    this.localStorageService.createLocalStorage(session, StorageKey.MAINT_SIZE_SEARCH_CONDITION, this.maintSearchCondition);
  }

  /**
   * マスタテーブル情報初期化.
   */
  private initializeMaintTableData(): void {
    // 項目設定情報
    this.formConfigs = [];
    // 項目内容情報
    this.formValues = [];

    this.nextPageToken = '';
    this.pageNumber = 0;
  }

  /**
   * 検索処理を行う.
   * @param item 検索用モデル MaintCodeSearch
   * @returns 検索結果
   */
  private search(item: MaintSizeSearch): Observable<any> {
    //ブランドコードと、アイテムコードを結合(品種コード)
    //PRD_0156 #10669 mod start ：brandCDとitemCDが空白の場合、hscdに'zzzz'を設定
    //const hscd = (this.brandCD + this.itemCD);
    let hscd = null;
    if (!this.brandCD && !this.itemCD) {
      hscd = 'zzzz';
    } else {
      hscd = (this.brandCD + this.itemCD);
    }
    //PRD_0156 #10669 mod end
    this.saveSearchConditions(this.session);
    return this.maintSizeService.hsCode(hscd, item).pipe(
      tap(hsCode => {
        // データ
        this.maintCode = hsCode;
        if (hsCode.nextPageToken === undefined) {
          this.nextPageToken = null;
        } else {
          this.nextPageToken = hsCode.nextPageToken;
        }
      })
    );
  }

  /**
 * 検索結果に応じたメッセージをセットする.
 * @param results 検索結果
 */
  private setSearchResultMessage(results: any): void {
    if (results.length === 0) {
      this.message.footer.error = { code: 'INFO.RESULT_ZERO', param: null };
    }
  }

  /**
   * マスタデータ内の検索条件Model作成.
   * @param isReference 参照用か
   * @returns 検索用モデル MaintCodeSearch
   */
  private createMaintCodeSearchCondition(isReference: boolean): MaintCodeSearch {
    const x = {};

    // 参照処理でない場合、検索条件入力欄で値が入力されているものをxにいれる
    if (!isReference) {
      const form = this.searchForm.controls;
      Object.keys(form).forEach(key => {
        const formControl: FormControl = <FormControl> form[key];
        if (FormUtils.isNotEmpty(formControl.value)) {
          x[key] = formControl.value;
        }
      });
    }

    // 参照処理の場合、pageTokenはnullを設定
    return {
      pageToken: isReference ? null : this.nextPageToken,
      page: 0,
      maxResults: 100,
      conditions: x,
    } as MaintCodeSearch;
  }

  /**
    * チェックボックス(全体)変更時の処理.
    * @param checked チェック
    */
  onChangeDirect(checked: boolean): void {
    // 直送フラグ変更
    this.dataListFormArray.controls
      .filter(val => val.get('id').value !== "")
      .forEach(val => val.patchValue({ selected: checked }));
    // ボタン表示確認
    this.disableFooterButton();
  }


  /**
   * チェックボックス押下時の処理
   * @param index 位置
   * @param id idの値
   * @param selected チェック状態
   */
  onCheckbox(index: number, id: string, selected: boolean): void {
    if (selected) {
      // ボタン表示確認
      this.disableFooterButton();
    } else {
      if (FormUtils.isNotEmpty(id)) {
        // 更新行制御
        this.checkBoxEditLine(id);
      } else {
        // 追加行制御
        this.checkBoxNewLine(index, id);
      }
    }
  }

  /**
   * 更新行のチェックボックス制御処理
   * @param id ID
   */
  private checkBoxEditLine(id: string): void {
    // 入力更新されたか確認
    // 入力情報確認
    const formGroup: FormGroup = <FormGroup> this.dataListFormArray.controls.find(element => element.value.id === id);
    // 入力済の項目を探す
    const nowData = formGroup.value;
    // オリジナルデータを取得
    const orgData = this.formValues.find(element => element.id === id);
    // オリジナルデータと比較
    let checkFlag = true;
    Object.keys(orgData).forEach(key => {
      if (orgData[key] !== nowData[key]) {
        checkFlag = false;
      }
    });
    if (checkFlag) {
      // 変更済の項目なし
      // ボタン表示確認
      this.disableFooterButton();
    } else {
      // 変更済の項目あり
      const modalRef = this.openConfirmModal('TITLE.CANCEL_EIDT', 'INFO.CANCEL_EIDT_COMFIRM_MESSAGE');
      modalRef.result.then(
        (result: string) => {
          if (result === 'OK') {
            // 入力情報を修正処理
            Object.keys(formGroup.controls).forEach(key => {
              formGroup.controls[key].setValue(orgData[key]);
            });
            // ボタン表示確認
            this.disableFooterButton();
          }
        },
        () => {
          // チェックボックスを済に戻す
          formGroup.controls['selected'].setValue(true);
          // ボタン表示確認
          this.disableFooterButton();
        }
      );
    }
  }

  /**
   * 追加行のチェックボックス制御処理
   * @param index 位置
   * @param id ID
   */
  private checkBoxNewLine(index: number, id: string): void {
    // 追加行のチェックを外した場合の処理
    // 入力情報確認
    const formGroup = <FormGroup> this.dataListFormArray.at(index);
    // 入力済の項目を探す
    const found = Object.keys(formGroup.controls).filter(key => formGroup.controls[key].value !== '');
    // 初期値がある項目を削除
    const filterData = found.filter(key => key !== 'id' && key !== 'tblid' && key !== 'selected');
    // 入力済の項目が存在するか確認
    if (filterData.length > 0) {
      // 入力済の項目がある
      const modalRef = this.openConfirmModal('TITLE.CANCEL_EIDT', 'INFO.CANCEL_EIDT_COMFIRM_MESSAGE');
      // 確認モーダルのメッセージの戻り値を確認して、削除処理を行う。
      modalRef.result.then(
        (result: string) => {
          if (result === 'OK') {
            // 追加行削除処理
            this.dataListFormArray.removeAt(index);
            // ボタン表示確認
            this.disableFooterButton();
          }
        },
        () => {
          // チェックボックスを済に戻る
          formGroup.controls['selected'].setValue(true);
          // ボタン表示確認
          this.disableFooterButton();
        }
      );
    } else {
      // 入力済の項目がない
      // ボタン表示確認
      this.disableFooterButton();
    }
  }

  /**
   * チェックボックスによるフッターボタンの活性制御処理
   */
  private disableFooterButton(): void {
    // チェックありのデータ抽出
    const selectedData = this.dataListFormArray.controls.filter((v) => v.value.selected === true);
    const isExistingData = selectedData.some((v) => FormUtils.isNotEmpty(v.value.id));
    if (selectedData.length === 0) {
      // チェックなし
      this.deleteButtonDisabled = true;
      this.updateButtonDisabled = true;
    } else if (isExistingData) {
      // 既存行にチェックあり
      this.deleteButtonDisabled = false;
      this.updateButtonDisabled = false;
    } else {
      // 新規行にチェックあり
      this.deleteButtonDisabled = true;
      this.updateButtonDisabled = false;
    }
  }

  /**
   * 追加ボタン押下時の処理
   */
  onNewEntry(): void {
    // 追加時の新行情報
    const newLine: any = [];
    this.formConfigs.forEach(v => {
      if (v.key === 'id') {
        newLine[v.key] = null; //#10671
      } else if (v.key === 'tblid') {
        newLine[v.key] = this.searchTblId;
      } else {
        newLine[v.key] = '';
      }
    });
    newLine['selected'] = true;

    // 先頭行に新規行を追加
    this.dataListFormArray.insert(0, this.createCodeDataFormGroup(newLine));
    // ボタン表示確認
    this.disableFooterButton();
  }

  /**
   * 更新ボタン押下時の処理
   */
  onUpdate(): void {
    this.submitted = true;
    let loadingToken = null;
    let isError = false;

    // ローディング開始
    this.loadingService.start().pipe(
      // ローディングトークン待避
      tap((token) => loadingToken = token),
      // メッセージをクリア
      tap(() => this.clearMessage()),
      // バリデーションが正常の場合、処理を続行
      filter(() => this.isValid()),

      // 項目内容情報
      tap(() => this.formValues = this.maintCode.items),
      filter(() => this.checkUpdate()),
      // 更新処理
      flatMap(() => this.update(this.createUpdateCode())),
      // 前回検索した条件で再表示
      flatMap(() => this.search(this.maintSearchItem)),
      tap(() => this.formValues = this.maintCode.items),
      tap(() => this.dataForm = this.createDataFormGroup()),
      catchError((error) => {
        //isError = true;
        return this.messageConfirmModalService.openErrorModal(error);
      }),
      finalize(() => {
        // 画面表示（エラーが発生した場合は、非表示のままとする）
        this.invisibledDataForm = isError;
        // ボタン表示確認
        this.disableFooterButton();
        // ローディング停止
        this.loadingService.stop(loadingToken);
      })
    ).subscribe();
  }

  /**
   * コピー新規ボタン押下時の処理
   */
  onCopy(): void {
    this.submitted = true;
    let loadingToken = null;
    let isError = false;

    // ローディング開始
    this.loadingService.start().pipe(
      // ローディングトークン待避
      tap((token) => loadingToken = token),
      // メッセージをクリア
      tap(() => this.clearMessage()),
      // バリデーションが正常の場合、処理を続行
      filter(() => this.isValid()),
      // コピー先確認
      filter(() => this.checkCopy()),
      // PRD_0165 #10699 add start
      // ブランド、アイテム欄空白チェック
      filter(() => this.checkHscdBlank()),
      // PRD_0165 #10669 add end
      // 項目内容情報
      tap(() => this.formValues = this.maintCode.items),
      // 更新処理
      flatMap(() => this.copy(this.createCopyCode())),
      // 前回検索した条件で再表示
      flatMap(() => this.search(this.maintSearchItem)),
      tap(() => this.formValues = this.maintCode.items),
      tap(() => this.dataForm = this.createDataFormGroup()),
      catchError((error) => {
        //isError = true;
        return this.messageConfirmModalService.openErrorModal(error);
      }),
      finalize(() => {
        // 画面表示（エラーが発生した場合は、非表示のままとする）
        this.invisibledDataForm = isError;
        // ボタン表示確認
        this.disableFooterButton();
        // ローディング停止
        this.loadingService.stop(loadingToken);
      })
    ).subscribe();
  }

  /**
   * バリデーションの結果を取得する。バリデーションエラーがある場合、フッターにバリデーションエラーメッセージを表示する。
   * @returns バリデーションの結果
   * - true : 正常
   * - false : エラーあり
   */
  private isValid(): boolean {//#10671
    // チェックありレコードのバリデーションチェック
    const isInvalid = this.dataListFormArray.controls
      .filter(val => val.get('selected').value === true)
      .filter(val => val.get('szkg').value !== "")
      .filter(val => val.get('jun').value !== "")
      .some(val => val.invalid);


    if (isInvalid) {
      this.message.footer.error = { code: 'ERRORS.VALID_ERROR', param: null };
      return false;
    }

    let updateData = this.dataListFormArray.controls
      .filter(val => val.get('selected').value === true)
    // PRD_0157 #10669 mod JFE start
      //.filter(val => val.get('szkg').value.length !== 0)
      //.filter(val => val.get('jun').value.length !== 0)
      .filter(val => val.get('szkg').value.length == 0 || val.get('jun').value.length == 0)
    // PRD_0157 #10669 mod JFE end
      .map(val => val.value);

    // PRD_0157 #10669 mod JFE start
    //if (updateData.length == 0) {
    if (updateData.length !== 0) {
    // PRD_0157 #10669 mod JFE end
      this.message.footer.error = { code: '登録内容を確認してください。', param: null };
      return false;
    }
    return true;
  }

  /**
   * 更新対象のデータ生成.
   */
  private createUpdateCode(): UpdateCode {
    let updateData = [];
    const storageCondition2: MaintSizeSearchCondition = JSON.parse(
      this.localStorageService.getSaveLocalStorage(this.session, StorageKey.MAINT_SIZE_SEARCH_CONDITION));
    const brandcd = storageCondition2.brandCode;
    const itemcd = storageCondition2.itemCode;
    const hscd = brandcd + itemcd;


    // チェックされているものを抽出(selected要素なし)
    updateData = this.dataListFormArray.controls
      .filter(val => val.get('selected').value === true)
      .filter(val => val.get('szkg').value !== "")
      .filter(val => val.get('jun').value !== "")
      .map(val => val.value);

    return {
      // revisionedAt: this.revisionedAt,
      hscd: hscd,
      revisionedAt: new Date,
      items: updateData
    } as UpdateCode;
  }

  /**
 * コピー新規対象のデータ生成.
 */
  private createCopyCode(): UpdateCode {
    let updateData = [];
    // const storageCondition2: MaintSizeSearchCondition = JSON.parse(
    //   this.localStorageService.getSaveLocalStorage(this.session, StorageKey.MAINT_SIZE_SEARCH_CONDITION));
    // const brandcd = storageCondition2.brandCode;
    // const itemcd = storageCondition2.itemCode;
    const hscd = (this.brandCD + this.itemCD);

    // チェックされているものを抽出(selected要素なし)
    updateData = this.dataListFormArray.controls
      .filter(val => val.get('selected').value === true)
      .filter(val => val.get('szkg').value !== "")
      .filter(val => val.get('jun').value !== "")
      .map(val => val.value);

    return {
      // revisionedAt: this.revisionedAt,
      hscd: hscd,
      revisionedAt: new Date,
      items: updateData
    } as UpdateCode;
  }

  /**
 * kピー新規先の品種が設定されているかをクリアする。
 */
  private checkCopy(): boolean {
    const hscd = (this.brandCD + this.itemCD);
    const storageCondition2: MaintSizeSearchCondition = JSON.parse(
      this.localStorageService.getSaveLocalStorage(this.session, StorageKey.MAINT_SIZE_SEARCH_CONDITION));
    const brandcd = storageCondition2.brandCode;
    const itemcd = storageCondition2.itemCode;

    if (hscd == (brandcd + itemcd)) {
      this.message.footer.error = { code: 'ERRORS.HSCD_ERROR', param: null };
      return false;
    }
    return true;
  }

    /**
 * kピー新規先の品種が設定されているかをクリアする。
 */
    private checkUpdate(): boolean {
      const hscd = (this.brandCD + this.itemCD);
      const storageCondition2: MaintSizeSearchCondition = JSON.parse(
        this.localStorageService.getSaveLocalStorage(this.session, StorageKey.MAINT_SIZE_SEARCH_CONDITION));
      const brandcd = storageCondition2.brandCode;
      const itemcd = storageCondition2.itemCode;

      if (hscd !== (brandcd + itemcd)) {
        this.message.footer.error = { code: '品種が変更されています。再度検索してください。', param: null };
        return false;
      }
      return true;
    }

  /**
   * 更新処理実施.
   * @param item 更新用モデル UpdateCode
   * @returns 結果
   */
  private update(item: UpdateCode): Observable<any> {
    this.maintCodeSettings = null;
    return this.maintSizeService.update(item).pipe(
      tap(maintCode => {
        this.result = maintCode;
        if (Object.keys(this.result).length > 0) {
          if (Object.keys(this.result)[0] === 'success') {
            this.message.footer.success = { code: 'SUCSESS.CODE_UPDATE', param: null };
          } else {
            this.message.footer.error = { code: 'ERRORS.ANY_ERROR', param: null };
          }
        }
      }),
    );
  }

  /**
   * コピー新規処理実施.
   * @param item 更新用モデル UpdateCode
   * @returns 結果
   */
  private copy(item: UpdateCode): Observable<any> {
    this.maintCodeSettings = null;
    return this.maintSizeService.copy(item).pipe(
      tap(maintCode => {
        this.result = maintCode;
        if (Object.keys(this.result).length > 0) {
          if (Object.keys(this.result)[0] === 'success') {
            this.message.footer.success = { code: 'SUCSESS.CODE_COPYUPDATE', param: null };
          } else {
            this.message.footer.error = { code: 'ERRORS.ANY_ERROR', param: null };
          }
        }
      }),
    );
  }



  /**
   * 削除ボタン押下時の処理.
   */
  onDelete(): void {
    let loadingToken = null;
    let isError = false;

    // 削除対象データ
    const deleteData = this.createDeleteCode();
    // チェックされている既存行数
    const deleteCount = deleteData.items.length;

    // 削除確認モーダル表示
    const modalRef = this.openConfirmModal('TITLE.DELETE', 'INFO.DELETE_CODE_MESSAGE', { value: deleteCount });
    // 確認モーダルのメッセージの戻り値を確認して、削除処理を行う。
    modalRef.result.then((result: string) => {
      // ローディング開始
      this.loadingService.start().pipe(
        // ローディングトークン待避
        tap((token) => loadingToken = token),
        // メッセージをクリア
        tap(() => this.clearMessage()),
        // 画面非表示
        tap(() => this.invisibledDataForm = true),
        // 削除処理
        flatMap(() => this.delete(deleteData, deleteCount)),
        // 前回検索した条件で再表示
        flatMap(() => this.search(this.maintSearchItem)),
        tap(() => this.formValues = this.maintCode.items),
        tap(() => this.dataForm = this.createDataFormGroup()),
        // エラーが発生した場合は、エラーモーダルを表示
        catchError((error) => {
          isError = true;
          return this.messageConfirmModalService.openErrorModal(error);
        }),
        finalize(() => {
          // 画面表示（エラーが発生した場合は、非表示のままとする）
          this.invisibledDataForm = isError;
          // ボタン表示確認
          this.disableFooterButton();
          // ローディング停止
          this.loadingService.stop(loadingToken);
        })
      ).subscribe();
    },
      () => {
        // ×ボタン押下時、処理なし
      });
  }

  /**
   * 削除対象のデータ生成.
   */
  private createDeleteCode(): UpdateCode {
    let deleteData = [];
    // チェックされているものを抽出(selected要素なし)
    // ※新規追加行は削除対象に含まない
    deleteData = this.dataListFormArray.controls
      .filter(val => FormUtils.isNotEmpty(val.get('id').value) && val.get('selected').value === true)
      .map(val => val.value);
    return {
      revisionedAt: new Date,
      items: deleteData
    } as UpdateCode;
  }

  /**
   * 削除処理実施.
   * @param item 削除対象データ
   * @param deleteCount 削除レコード数
   * @returns 結果
   */
  private delete(item: UpdateCode, deleteCount: number): Observable<any> {
    return this.maintSizeService.delete(item)
      .pipe(
        tap(maintDelete => {
          this.result = maintDelete;
          if (Object.keys(this.result).length > 0) {
            if (Object.keys(this.result)[0] === 'success') {
              this.message.footer.success = { code: 'SUCSESS.DELTE', param: { value: deleteCount } };
            } else {
              this.message.footer.error = { code: 'ERRORS.ANY_ERROR', param: null };
            }
          }
        }),
      );
  }
// PRD_0165 #10669 add start
  /**
   * 品種コード空白チェック.
   * @returns boolean
   */
  private checkHscdBlank():boolean {
    if (this.maintForm.controls['brandCode'].value == ""
      || this.maintForm.controls['itemCode'].value == "") {
      this.message.footer.error = { code: 'ブランド欄、またはアイテム欄が空白です。', param: null };
      return false
    }
    else return true
  }
  // PRD_0165 #10669 add end
}
//PRD_0137 #10669 add end
