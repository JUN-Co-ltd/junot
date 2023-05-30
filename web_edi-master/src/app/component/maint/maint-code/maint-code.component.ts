import { Component, OnInit, OnDestroy } from '@angular/core';
import { MaintCodeService } from 'src/app/service/maint/maint-code.service';
import { MaintCodeResult } from 'src/app/model/maint/maint-code-result';
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
import { StringUtils } from 'src/app/util/string-utils';
import { GenericList } from 'src/app/model/generic-list';
import { ScrollEvent } from 'ngx-scroll-event';
import { ListUtils } from 'src/app/util/list-utils';

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
  selector: 'app-maint-code',
  templateUrl: './maint-code.component.html',
  styleUrls: ['./maint-code.component.scss']
})

export class MaintCodeComponent implements OnInit, OnDestroy {
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

  /** 対象マスタテーブル検索用フォーム */
  maintForm: FormGroup;
  /** マスタデータ内検索用フォーム */
  searchForm: FormGroup;
  /** マスタデータ用フォーム */
  dataForm: FormGroup;

  /** マスタテーブル一覧 */
  maintTableList: MaintCodeResult[] = [];
  /** 指定メンテコード画面構成情報 */
  maintCodeSettings: MaintTableInfoResult;
  /** 指定メンテコード情報リスト */
  maintCode: any;
  /** 更新、削除結果 */
  result: any;

  // /** 削除POSTmodel */
  // deleteModel: UpdateCode;

  maintSearchItem: MaintCodeSearch;

  /** 項目設定情報 */
  formConfigs = [];
  /** 項目内容情報 */
  formValues = [];
  /** チェックボックスの回帰抑止 */
  checkboxCtrl = false;

  // 次のページのトークン
  nextPageToken: string;
  // ページ番号
  pageNumber: number;
  // Submit押下
  submitted = false;

  private loadingSubscription: Subscription;
  private revisionedAt: string | Date;

  constructor(
    private maintCodeService: MaintCodeService,
    private messageConfirmModalService: MessageConfirmModalService,
    private headerService: HeaderService,
    private loadingService: LoadingService,
    private modal: NgbModal,
    private formBuilder: FormBuilder,
    private translateService: TranslateService,
  ) { }

  ngOnInit() {
    this.headerService.show();

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
      // マスタメンテ コード管理テーブル一覧取得(画面構成マスタデータ)
      flatMap(() => this.generateMaintTableList()),
      // 対象マスタテーブル検索用フォームを生成
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
   * マスタテーブル一覧を取得する.
   * @returns マスタテーブル一覧 MaintCodeResult
   */
  private generateMaintTableList(): Observable<GenericList<MaintCodeResult>> {
    this.maintTableList = []; // 初期化
    return this.maintCodeService.getTable().pipe(
      tap(maintCode => this.maintTableList = maintCode.items));
  }

  /**
   * エラー表示の有無を返却する。
   * @param value FormControl
   */
  getErrors(valueGroup: FormGroup): Set<TranslateEx> {
    const errors = new Set<TranslateEx>();
    Object.keys(valueGroup.controls).forEach(key => {
      // エラー判定
      if (this.isErrorDisplay(<FormControl> valueGroup.controls[key])) {
        Object.keys(valueGroup.controls[key].errors).forEach(errorKey => {
          // 項目情報名の取得
          const list = this.formConfigs.filter(val => key === val.key);
          if (errorKey === 'required') {
            // バリデーション：必須
            errors.add({ code: 'ERRORS.VALIDATE.ANY_EMPTY', params: { name: list[0].name } } as TranslateEx);
          } else if (errorKey === 'pattern') {
            // バリデーション：正規表現
            errors.add({ code: 'ERRORS.VALIDATE.PATTERN_PROHIBITED_CHAR', params: { name: list[0].name } } as TranslateEx);
          } else if (errorKey === 'minlength') {
            // バリデーション：最小入力文字数
            if (list[0].validators.maxLength === list[0].validators.maxLength) {
              // 固定文字数エラー
              errors.add({
                code: 'ERRORS.VALIDATE.LENGTH_NOT_MATCH',
                params: { name: list[0].name, length: list[0].validators.minLength }
              } as TranslateEx);
            } else {
              errors.add({
                code: 'ERRORS.VALIDATE.LENGTH_SHORTAGE', params: { name: list[0].name, length: list[0].validators.minLength }
              } as TranslateEx);
            }
          } else if (errorKey === 'maxlength') {
            // バリデーション：最大入力文字数
            if (list[0].validators.maxLength === list[0].validators.maxLength) {
              // 固定文字数エラー
              errors.add({
                code: 'ERRORS.VALIDATE.LENGTH_NOT_MATCH', params: { name: list[0].name, length: list[0].validators.maxLength }
              } as TranslateEx);
            } else {
              errors.add({
                code: 'ERRORS.VALIDATE.LENGTH_EXCEED', params: { name: list[0].name, length: list[0].validators.maxLength }
              } as TranslateEx);
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
      selectTblId: '' // テーブル区分
    });

    // マスタメンテ コード管理テーブル一覧がない場合は以降処理しない
    // TODO エラーにするか？
    if (ListUtils.isEmpty(this.maintTableList)) {
      return f;
    }

    // マスタメンテ コード管理テーブル一覧がある場合は先頭のテーブル区分をセット
    f.controls.selectTblId.patchValue(this.maintTableList[0].tableId);
    return f;
  }

  /**
   * マスタデータ内検索用FromGroupを作成する.
   * @returns 検索領域のFormGroup
   */
  private createSearchFormGroup(): FormGroup {
    // 空のマスタデータ内検索用FromGroup作成
    // keyの数ぶん検索項目がある
    const formControls = {} as FormControl[];
    this.formConfigs.forEach(v => {
      formControls[v.key] = new FormControl();
    });

    return this.formBuilder.group(formControls);
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

    this.formConfigs.forEach(v => {
      const validatorFn: ValidatorFn[] = [];

      // バリデーションを動的に定義する
      if (v.validators) {
        //PRD_0147 #10671 mod JFE start
        // if (v.validators.required === 'true') {
        if (v.validators.required === 'true' && rowData.id !== null) {
          //PRD_0147 #10671 mod JFE end
          // バリデーション：必須
          validatorFn.push(Validators.required);
        }
        if (v.validators.maxLength) {
          // バリデーション：最大文字数
          validatorFn.push(Validators.maxLength(v.validators.maxLength));
        }
        if (v.validators.minLength) {
          // バリデーション：最小文字数
          validatorFn.push(Validators.minLength(v.validators.minLength));
        }
        if (v.validators.pattern) {
          // バリデーション：正規表現
          validatorFn.push(Validators.pattern(v.validators.pattern));
        }
      }

      // FormControl生成（値,{ validators:バリデーション,updateOn:トリガー})
      formControls[v.key] = new FormControl(rowData[v.key], { validators: validatorFn, updateOn: 'blur' });
    });

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
   * 参照ボタン押下時の処理.
   *
   * 指定されたテーブル区分のマスタデータを取得する
   * ※編集しているセルがある場合は確認モーダルを表示する
   */
  onReference(): void {
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
   * 指定されたテーブル区分のマスタデータを参照する.
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
      // 項目設定情報取得
      flatMap(() => this.getScreenSettings()),
      tap(() => this.formConfigs = this.maintCodeSettings.fields),
      // 対象マスタデータ内の検索条件Model作成
      tap(() => this.maintSearchItem = this.createMaintCodeSearchCondition(true)),
      flatMap(() => this.search(this.maintSearchItem)),
      tap(() => this.formValues = this.maintCode.items),
      // フォーム作成
      tap(() => {
        this.searchForm = this.createSearchFormGroup();
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
   * 指定メンテコード画面構成情報を取得する。
   * @returns 画面構成情報
   */
  private getScreenSettings(): Observable<MaintTableInfoResult> {
    this.maintCodeSettings = null; // 初期化

    return this.maintCodeService.screenSettings(this.searchTblId).pipe(
      tap(maintSettings => this.maintCodeSettings = maintSettings.item)
    );
  }

  /**
   * 検索処理を行う.
   * @param item 検索用モデル MaintCodeSearch
   * @returns 検索結果
   */
  private search(item: MaintCodeSearch): Observable<any> {
    return this.maintCodeService.maintCode(this.searchTblId, item).pipe(
      tap(maintCode => {
        // データ
        this.maintCode = maintCode;
        // 改訂日時
        this.revisionedAt = maintCode.revisionedAt;
        if (maintCode.nextPageToken === undefined) {
          this.nextPageToken = null;
        } else {
          this.nextPageToken = maintCode.nextPageToken;
        }
      })
    );
  }

  /**
   * スクロール処理.
   * 最下部までスクロールされた場合は、次の検索結果を取得する
   * @param event ScrollEvent
   */
  onScroll(event: ScrollEvent): void {
    if (event.isWindowEvent || !event.isReachingBottom || StringUtils.isEmpty(this.nextPageToken)) {
      return;
    }
    let loadingToken = null;
    // ローディング開始
    this.loadingService.start().pipe(
      // ローディングトークン待避
      tap((token) => loadingToken = token),
      // 次ページ項目内容情報取得
      flatMap(() => this.search(this.pageNext())),
      tap(() => this.pushDataFormGroup(this.maintCode.items)),
      // エラーが発生した場合は、エラーモーダルを表示
      catchError((error) => this.messageConfirmModalService.openErrorModal(error)),
      // ローディング停止
      finalize(() => this.loadingService.stop(loadingToken))
    ).subscribe();
  }

  /**
   * コードデータ領域FormArrayに次ページデータを追加.
   * @param items 次ページデータ
   */
  private pushDataFormGroup(items: any): void {
    // データ（行）をループ
    items.forEach(v => {
      v['selected'] = false;
      // 画面要素追加（チェックボックス）
      this.formValues.push(v);
      this.dataListFormArray.push(this.createCodeDataFormGroup(v));
    });
  }

  /**
   * 検索ボタン押下時の処理.
   *
   * マスタデータ内を検索する
   * ※編集しているセルがある場合は確認モーダルを表示する
   */
  onSearch(): void {
    // 編集しているセルがあればモーダルを表示する。
    if (this.isDirty()) {
      const modalRef = this.openConfirmModal('TITLE.CANCEL_EIDT', 'INFO.CANCEL_EIDT_COMFIRM_MESSAGE');
      modalRef.result.then((result: string) => {
        this.searchMaintCode();
      },
        () => {
          // ×ボタン押下時、処理なし
        });
    } else {
      this.searchMaintCode();
    }
  }

  /**
   * マスタデータ内を検索する.
   */
  private searchMaintCode(): void {
    let loadingToken = null;
    let isError = false;

    this.nextPageToken = '';
    this.pageNumber = 0;

    // ローディング開始
    this.loadingService.start().pipe(
      // ローディングトークン待避
      tap((token) => loadingToken = token),
      // 画面非表示
      tap(() => this.invisibledDataForm = true),
      // メッセージをクリア
      tap(() => this.clearMessage()),
      // 検索条件
      tap(() => this.maintSearchItem = this.createMaintCodeSearchCondition(false)),
      // 検索処理
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
      conditions: x
    } as MaintCodeSearch;
  }

  /**
   * スクロール次ページ要求内容
   * @returns 次ページ検索用モデル MaintCodeSearch
   */
  private pageNext(): MaintCodeSearch {
    this.pageNumber = this.pageNumber + 1;
    return {
      pageToken: this.nextPageToken,
      page: this.pageNumber,
      maxResults: 100,
      conditions: {}
    } as MaintCodeSearch;
  }

  /**
   * チェックボックス押下時の処理
   * @param index 位置
   * @param id idの値
   * @param selected チェック状態
   */
  onCheckbox(index: number, id: number, selected: boolean): void {
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
   * @param id idの値
   */
  private checkBoxEditLine(id: number): void {
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
   * @param id idの値
   */
  private checkBoxNewLine(index: number, id: number): void {
    // 追加行のチェックを外した場合の処理
    // 入力情報確認
    const formGroup = <FormGroup> this.dataListFormArray.controls.find(element => element.value.id === id);
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
      // 追加行削除処理
      this.dataListFormArray.removeAt(index);
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
        newLine[v.key] = null;
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
      // 画面非表示
      tap(() => this.invisibledDataForm = true),
      // 項目内容情報
      tap(() => this.formValues = this.maintCode.items),
      // 更新処理
      flatMap(() => this.update(this.createUpdateCode())),
      // 前回検索した条件で再表示
      flatMap(() => this.search(this.maintSearchItem)),
      tap(() => this.formValues = this.maintCode.items),
      tap(() => this.dataForm = this.createDataFormGroup()),
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
   * バリデーションの結果を取得する。バリデーションエラーがある場合、フッターにバリデーションエラーメッセージを表示する。
   * @returns バリデーションの結果
   * - true : 正常
   * - false : エラーあり
   */
  private isValid(): boolean {
    // チェックありレコードのバリデーションチェック
    const isInvalid = this.dataListFormArray.controls
      .filter(val => val.get('selected').value === true)
      .some(val => val.invalid);

    if (isInvalid) {
      this.message.footer.error = { code: 'ERRORS.VALID_ERROR', param: null };
      return false;
    }

    return true;
  }

  /**
   * 更新対象のデータ生成.
   */
  private createUpdateCode(): UpdateCode {
    let updateData = [];
    // チェックされているものを抽出(selected要素なし)
    updateData = this.dataListFormArray.controls
      .filter(val => val.get('selected').value === true)
      .map(val => val.value);
    return {
      revisionedAt: this.revisionedAt,
      items: updateData
    } as UpdateCode;
  }

  /**
   * 更新処理実施.
   * @param item 更新用モデル UpdateCode
   * @returns 結果
   */
  private update(item: UpdateCode): Observable<any> {
    this.maintCodeSettings = null;
    return this.maintCodeService.update(this.searchTblId, item).pipe(
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
      revisionedAt: this.revisionedAt,
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
    return this.maintCodeService.delete(this.searchTblId, item)
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
}
