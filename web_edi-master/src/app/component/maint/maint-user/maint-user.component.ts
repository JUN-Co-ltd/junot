import { Component, OnInit, OnDestroy } from '@angular/core';
import { FormGroup, FormBuilder, FormControl, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { TranslateService } from '@ngx-translate/core';
import { Observable, of, from, combineLatest, Subscription } from 'rxjs';
import { tap, flatMap, filter, catchError, finalize } from 'rxjs/operators';

import { SearchSupplierModalComponent } from 'src/app/component/search-supplier-modal/search-supplier-modal.component';
import {
  SubmitType, Path, PreEventParam, SupplierType, AuthType, RegistStatus, SearchTextType,
  ValidatorsPattern
} from 'src/app/const/const';
import { MaintUser } from 'src/app/model/maint/maint-user';
import { HeaderService } from 'src/app/service/header.service';
import { LoadingService } from 'src/app/service/loading.service';
import { MessageConfirmModalService } from 'src/app/service/message-confirm-modal.service';
import { MaintUserService } from 'src/app/service//maint/maint-user.service';
import { NumberUtils } from 'src/app/util/number-utils';
import { FormUtils } from 'src/app/util/form-utils';
import { StringUtils } from 'src/app/util/string-utils';
import { ObjectUtils } from 'src/app/util/object-utils';

import { JunpcSirmstSearchCondition } from 'src/app/model/junpc-sirmst-search-condition';
import { JunpcSirmst } from 'src/app/model/junpc-sirmst';
import { JunpcSirmstService } from 'src/app/service/junpc-sirmst.service';
import { Authority } from 'src/app/enum/authority.enum';

@Component({
  selector: 'app-maint-user',
  templateUrl: './maint-user.component.html',
  styleUrls: ['./maint-user.component.scss']
})
export class MaintUserComponent implements OnInit, OnDestroy {
  readonly SUBMIT_TYPE = SubmitType;
  readonly AUTH_SUPPLIERS = AuthType.AUTH_SUPPLIERS;
  readonly SUPPLIER_TYPE = SupplierType;
  readonly REGIST_STATUS = RegistStatus;
  readonly PATH = Path;
  readonly AUTHORITY_TYPES = {
    ADMIN : Authority.ROLE_USER + ',' + Authority.ROLE_JUN + ',' + Authority.ROLE_EDI + ',' + Authority.ROLE_ADMIN,
    JUN: Authority.ROLE_USER + ',' + Authority.ROLE_JUN + ',' + Authority.ROLE_EDI,
    DISTA: Authority.ROLE_USER + ',' + Authority.ROLE_JUN + ',' + Authority.ROLE_DISTA,
    MAKER : Authority.ROLE_USER + ',' + Authority.ROLE_MAKER
  };
  readonly AUTHORITY = Authority;

  /** 画面を非表示にする */
  invisibled = true;
  /** 画面を非活性にする */
  disabled = true;
  /** フォーム */
  mainForm: FormGroup;
  /** タイトル */
  title: string;
  /** パス（画面の表示モード） */
  path = '';
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

  /** 会社コードを非活性にする */
  companyCodeDisabled = true;

  /** ローディングサブスクリプション */
  private loadingSubscription: Subscription;

  constructor(
    private router: Router,
    private route: ActivatedRoute,
    private formBuilder: FormBuilder,
    private translateService: TranslateService,
    private modal: NgbModal,
    private headerService: HeaderService,
    private loadingService: LoadingService,
    private messageConfirmModalService: MessageConfirmModalService,
    private maintUserService: MaintUserService,
    private junpcSirmstService: JunpcSirmstService,
  ) { }

  ngOnInit() {
    this.headerService.show();

    // ローディングクリア（親画面のみ）
    this.loadingService.clear();

    // ローディングサブスクリプション開始
    this.loadingSubscription = this.loadingService.isLoading$.subscribe((isLoading) => this.disabled = isLoading);

    // URLとクエリパラメータの変更を検知
    combineLatest([
      this.route.paramMap,
      this.route.queryParamMap
    ]).subscribe(([paramMap, queryParamMap]) => {
      // URLの最後のパス（画面の表示モード）を取得
      this.path = this.route.snapshot.url[this.route.snapshot.url.length - 1].path;
      // URLからidを取得
      const id = NumberUtils.toNumberDefaultIfEmpty(paramMap.get('id'), null);
      // クエリパラメータからpreEvent(遷移前の処理)を取得
      const preEvent = NumberUtils.toNumberDefaultIfEmpty(queryParamMap.get('preEvent'), null);

      let loadingToken = null;
      let isError = false;

      // ローディング開始
      this.loadingService.start().pipe(
        // ローディングトークン待避
        tap((token) => loadingToken = token),
        // メッセージをクリア
        tap(() => this.clearMessage()),
        // 画面非表示
        tap(() => this.invisibled = true),
        // フォームを生成
        tap(() => this.mainForm = this.createFormGroup()),
        // 画面表示に必要な翻訳テキストを取得
        flatMap(() => this.getInitialTranslate()),
        // preEvent(遷移前の処理)によってメッセージを設定
        tap(() => this.setPreEventMessage(preEvent)),
        // idが設定されている場合は、処理を続行
        filter(() => id !== null),
        // ユーザを取得
        flatMap(() => this.getMaintUser(id)),
        // 権限により会社コードの活性・非活性を設定
        tap(() => this.onChangeAuthority(this.f.authorities.value)),
        // エラーが発生した場合は、エラーモーダルを表示
        catchError((error) => {
          isError = true;
          return this.messageConfirmModalService.openErrorModal(error);
        }),
        finalize(() => {
          // 画面表示（エラーが発生した場合は、非表示のままとする）
          this.invisibled = isError;
          // ローディング停止
          this.loadingService.stop(loadingToken);
        })
      ).subscribe();
    });
  }

  ngOnDestroy() {
    // ローディングサブスクリプション停止
    this.loadingSubscription.unsubscribe();
  }

  /**
   * フォームを取得する。
   * @return mainForm.controls
   */
  get f(): any { return this.mainForm.controls; }

  /**
   * エラー表示の有無を返却する。
   * @param value FormControl
   */
  isErrorDisplay(value: FormControl): boolean {
    return value.invalid && (value.dirty || value.touched);
  }

  /**
   * バリデーションの結果を取得する。バリデーションエラーがある場合、フッターにバリデーションエラーメッセージを表示する。
   * @returns バリデーションの結果
   * - true : 正常
   * - false : エラーあり
   */
  private isValid(): boolean {
    if (this.mainForm.invalid) {
      this.message.footer.error = { code: 'ERRORS.VALID_ERROR', param: null };
      FormUtils.markAsTouchedAllFields(this.mainForm);
      return false;
    }

    return true;
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
   * 画面表示に必要な翻訳テキストを取得する。
   */
  private getInitialTranslate(): Observable<any> {
    return this.translateService.get('TITLE.USER').pipe(
      tap((title) => {
        // タイトルを取得
        this.title = title;
      }
      ));
  }

  /**
   * preEvent(遷移前の処理)によってメッセージを設定する。
   * @param preEvent URLクエリパラメータpreEvent(遷移前の処理)
   */
  private setPreEventMessage(preEvent: number): void {
    switch (preEvent) {
      case PreEventParam.CREATE:
        this.message.footer.success = { code: 'SUCSESS.ENTRY', param: { value: this.title } };
        break;
      case PreEventParam.UPDATE:
        this.message.footer.success = { code: 'SUCSESS.UPDATE', param: { value: this.title } };
        break;
      default:
        this.message.footer.success = { code: '', param: null };
        break;
    }
  }

  /**
   * フォームを作成する。
   */
  private createFormGroup(): FormGroup {
    const f = this.formBuilder.group({
      // ID
      id: [null],
      // ログインID
      accountName: [null, { validators: [Validators.pattern(ValidatorsPattern.NUMERIC_0_9)], updateOn: 'blur' }],
      // パスワード
      password: [null, { updateOn: 'blur' }],
      // 名前
      name: [null],
      // 権限
      authorities: [null, { updateOn: 'blur' }],
      // メールアドレス
      mailAddress: [null, { validators: [Validators.pattern(ValidatorsPattern.EMAIL_COMMA_DELIMITED)], updateOn: 'blur' }],
      // ログインの有効/無効
      enabled: [true],
      // 会社コード
      companyCode: [null, { validators: [Validators.pattern(ValidatorsPattern.NUMERIC_0_9)], updateOn: 'blur' }],
      // メーカー名称
      makerName: [null]
    },
      {
        validator: Validators.compose([
        ])
      }
    );

    // 会社コードを非活性状態にする
    f.controls.companyCode.disable();
    this.companyCodeDisabled = true;

    return f;
  }

  /**
   * フォームに値を設定する。
   * @param maintUser 値
   */
  private setFormValue(maintUser: MaintUser) {
    this.mainForm.patchValue({
      id: maintUser.id,
      accountName: maintUser.accountName,
      password: maintUser.password,
      name: maintUser.name,
      authorities: this.convertAuthoritiesToScreen(maintUser.authorities),
      mailAddress: maintUser.mailAddress,
      enabled: maintUser.enabled,
      companyCode: maintUser.company,
      makerName: maintUser.makerName
    });
  }

  /**
   * フォームの値をもとに型変換した値を取得する。
   * @returns 型変換後の値
   */
  private convertFrom(): MaintUser {
    const formControls = this.f;

    return {
      // ID
      id: formControls.id.value,
      // ログインID
      accountName: formControls.accountName.value,
      // パスワード
      password: formControls.password.value,
      // 名前
      name: formControls.name.value,
      // 権限
      authorities: this.convertAuthoritiesToApi(formControls.authorities.value),
      // メールアドレス: 空白を削除
      mailAddress: StringUtils.deleteWhitespace(formControls.mailAddress.value),
      // ログインの有効/無効
      enabled: formControls.enabled.value,
      // 会社コード
      company: formControls.companyCode.value,
    } as MaintUser;
  }

  /**
   * 登録ボタン押下時の処理
   */
  onEntry(): void {
    let loadingToken = null;

    // ローディング開始
    this.loadingService.start().pipe(
      // ローディングトークン待避
      tap((token) => loadingToken = token),
      // メッセージをクリア
      tap(() => this.clearMessage()),
      // バリデーションが正常の場合、処理を続行
      filter(() => this.isValid()),
      // ユーザを作成
      flatMap(() => this.maintUserService.create(this.convertFrom())),
      // 遷移先のURLを設定
      tap((response) => this.router.navigate(
        ['maint/users', response.id, Path.EDIT],
        { queryParams: { preEvent: PreEventParam.CREATE, t: new Date().valueOf() } })),
      // エラーが発生した場合は、エラーモーダルを表示
      catchError((error) => this.messageConfirmModalService.openErrorModal(error)),
      // ローディング停止
      finalize(() => this.loadingService.stop(loadingToken))
    ).subscribe();
  }

  /**
   * 更新ボタン押下時の処理
   */
  onUpdate(): void {
    let loadingToken = null;

    // ローディング開始
    this.loadingService.start().pipe(
      // ローディングトークン待避
      tap((token) => loadingToken = token),
      // メッセージをクリア
      tap(() => this.clearMessage()),
      // バリデーションが正常の場合、処理を続行
      filter(() => this.isValid()),
      // ユーザを更新
      flatMap(() => this.maintUserService.update(this.convertFrom())),
      // 遷移先のURLを設定
      tap((response) => this.router.navigate(
        ['maint/users', response.id, Path.EDIT],
        { queryParams: { preEvent: PreEventParam.UPDATE, t: new Date().valueOf() } })),
      // エラーが発生した場合は、エラーモーダルを表示
      catchError((error) => this.messageConfirmModalService.openErrorModal(error)),
      // ローディング停止
      finalize(() => this.loadingService.stop(loadingToken))
    ).subscribe();
  }

  /**
   * 削除ボタン押下時の処理
   */
  onDelete(): void {
    let loadingToken = null;

    // 確認モーダル表示
    this.messageConfirmModalService.translateAndOpenConfirmModal('INFO.DELETE_COMFIRM_MESSAGE', { value: this.title }).pipe(
      // 確認モーダルの結果が「OK」の場合、処理を続行
      filter((result) => result),
      // ローディング開始＆トークン待避
      flatMap(() => loadingToken = this.loadingService.start()),
      // メッセージをクリア
      tap(() => this.clearMessage()),
      // ユーザを削除
      flatMap(() => this.maintUserService.delete(this.f.id.value)),
      // 遷移先のURLを設定
      tap(() => this.router.navigate(['maint/users'])),
      // エラーが発生した場合は、エラーモーダルを表示
      catchError((error) => this.messageConfirmModalService.openErrorModal(error)),
      // ローディング停止
      finalize(() => this.loadingService.stop(loadingToken))
    ).subscribe();
  }

  /**
   * ユーザを取得する。
   * @param userId ユーザID
   */
  private getMaintUser(userId: number): Observable<MaintUser> {
    return this.maintUserService.get(userId).pipe(
      tap((response) => this.setFormValue(response))
    );
  }

  /**
   * 権限の値をもとに画面の権限の値に変換する。
   * @param authorities 権限
   */
  private convertAuthoritiesToScreen(authorities: Authority[]): string {
    if (ObjectUtils.isNullOrUndefined(authorities)) {
      return '';
    } else if (authorities.indexOf(Authority.ROLE_ADMIN) > -1) {
      return this.AUTHORITY_TYPES.ADMIN;
    } else if (authorities.indexOf(Authority.ROLE_DISTA) > -1) {
      return this.AUTHORITY_TYPES.DISTA;
    } else if (authorities.indexOf(Authority.ROLE_JUN) > -1) {
        return this.AUTHORITY_TYPES.JUN;
    } else if (authorities.indexOf(Authority.ROLE_MAKER) > -1) {
      return this.AUTHORITY_TYPES.MAKER;
    }

    return '';
  }

  /**
   * 画面の権限の値を権限の値をもとにAPIに渡す値に変換する。
   * @param authorities 権限
   */
  private convertAuthoritiesToApi(authorities: string): Authority[] {
    if (StringUtils.isEmpty(authorities)) {
      return null;
    }

    return authorities.split(',') as Authority[];
  }

  /**
   * 選択した権限により、会社コードの活性・非活性を設定
   * @param value 権限
   */
  onChangeAuthority(value: string): void {
    if (this.AUTHORITY_TYPES.MAKER === value) {
      // メーカー権限が選択された場合、会社コードを活性状態にする
      this.mainForm.controls.companyCode.enable();
      this.companyCodeDisabled = false;

      return;
    } else {
      // メーカー権限以外が選択された場合、会社コードとメーカー名をクリアして、会社コードを非活性状態にする
      this.mainForm.patchValue({
        companyCode: null,
        makerName: ''
      });

      this.mainForm.controls.companyCode.disable();
      this.companyCodeDisabled = true;

      return;
    }
  }

  /**
   * 会社コードを検索するモーダルを表示する。
   * @param supplier 仕入先区分(会社コード)
   */
  openSearchSupplierModal(supplier: SupplierType): void {
    const modalRef = this.modal.open(SearchSupplierModalComponent);

    // モーダルへ渡す値を設定する。
    modalRef.componentInstance.searchCondition = {
      sirkbn: supplier,
      searchType: SearchTextType.CODE_NAME_PARTIAL_MATCH,
      searchText: this.f.companyCode.value
    } as JunpcSirmstSearchCondition;
    modalRef.componentInstance.default = { sire: this.f.companyCode.value } as JunpcSirmst;

    // モーダルからの値を設定する。
    from(modalRef.result).pipe(
      tap((result: JunpcSirmst) => {
        this.mainForm.patchValue({
          companyCode: result.sire,
          makerName: result.name
        });
      }),
      catchError(() => of(null)) // バツボタンクリック時は何もしない
    ).subscribe();
  }

  /**
   * `会社コード` 値変更時の処理。
   * @param value 値
   * @param supplierType 仕入先区分
   */
  onChangeMaker(value: string, supplierType: SupplierType): void {
    if (StringUtils.isEmpty(value) || value.length !== 5) {
      // メーカーの桁数が入力されていない場合は検索しない
      this.mainForm.patchValue({ makerName: null });
      return;
    }

    // 仕入れマスタ取得APIコール
    this.junpcSirmstService.getSirmst({
      sirkbn: supplierType,
      searchType: SearchTextType.CODE_PARTIAL_MATCH,
      searchText: value
    } as JunpcSirmstSearchCondition).subscribe(x => {
      // 結果が取得できない場合は初期化
      let makerName = null;
      if (x != null && x.items.length !== 0) {
        // 結果が取得出来たらそれぞれのName項目にセット
        makerName = x.items[0].name;
      }
      this.mainForm.patchValue({ makerName: makerName });
    });
  }

  /**
   * 'メールアドレス' フォーカスアウト時の処理。
   * @param value 入力値
   */
  onBlurMailAddress(value: string): void {
    this.mainForm.patchValue({ mailAddress: StringUtils.deleteWhitespace(value) });
  }
}
