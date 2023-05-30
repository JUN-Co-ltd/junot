import { Component, OnInit, OnDestroy } from '@angular/core';
import { FormGroup, FormBuilder, Validators } from '@angular/forms';
import { ScrollEvent } from 'ngx-scroll-event';
import { Observable, Subscription } from 'rxjs';
import { tap, flatMap, catchError, finalize, filter } from 'rxjs/operators';

import { Path } from 'src/app/const/const';
import { GenericList } from 'src/app/model/generic-list';
import { MaintUserSearchCondition } from 'src/app/model/maint/maint-user-search-condition';
import { MaintUserSearchResult } from 'src/app/model/maint/maint-user-search-result';
import { MaintUserService } from 'src/app/service/maint/maint-user.service';
import { HeaderService } from 'src/app/service/header.service';
import { LoadingService } from 'src/app/service/loading.service';
import { MessageConfirmModalService } from 'src/app/service/message-confirm-modal.service';
import { StringUtils } from 'src/app/util/string-utils';
import { Authority } from 'src/app/enum/authority.enum';
import { SearchMethod } from 'src/app/enum/search-method.enum';

@Component({
  selector: 'app-maint-user-list',
  templateUrl: './maint-user-list.component.html',
  styleUrls: ['./maint-user-list.component.scss']
})
export class MaintUserListComponent implements OnInit, OnDestroy {
  readonly PATH = Path;
  readonly KEYWORD_TYPES = {
    /** 未選択 */
    NO_SELECT: 0,
    /** ログインID */
    LOGIN_ID: 1,
    /** 会社コード */
    COMPANY_CODE: 2,
    /** メーカー */
    MAKER: 3,
    /** 名前 */
    NAME: 4,
    /** メールアドレス */
    MAIL_ADDRESS: 5
  };
  readonly AUTHORITY = Authority;
  readonly CONDITIONS_LIMIT = 10;

  /** 画面を非表示にする */
  invisibled = true;
  /** 画面を非活性にする */
  disabled = true;
  /** フォーム */
  mainForm: FormGroup;
  /** 画面に表示するメッセージ */
  message = {
    /** ボディ */
    body: {
      /** 異常系 */
      error: { code: '', param: null }
    }
  };
  /** 検索結果 */
  searchResultItems: MaintUserSearchResult[] = [];
  /** 次のページのトークン */
  nextPageToken: string;

  /** ローディングサブスクリプション */
  private loadingSubscription: Subscription;

  constructor(
    private formBuilder: FormBuilder,
    private headerService: HeaderService,
    private loadingService: LoadingService,
    private messageConfirmModalService: MessageConfirmModalService,
    private maintUserService: MaintUserService
  ) { }

  ngOnInit() {
    this.headerService.show();

    // ローディングクリア（親画面のみ）
    this.loadingService.clear();

    // ローディングサブスクリプション開始
    this.loadingSubscription = this.loadingService.isLoading$.subscribe((isLoading) => this.disabled = isLoading);

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
      // ユーザを検索
      flatMap(() => this.search()),
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
      body: {
        error: { code: '', param: null }
      }
    };
  }

  /**
   * フォームを作成する。
   */
  private createFormGroup(): FormGroup {
    return this.formBuilder.group({
      keywordType: [this.KEYWORD_TYPES.NO_SELECT],  // キーワードタイプ
      keyword: [''],     // キーワード
      authorityAdminSelected: [false],     // 権限: 管理者権限
      authorityJunSelected: [false],     // 権限: JUN権限
      authorityMakerSelected: [false],     // 権限: メーカー権限
      authorityDistaSelected: [false],     // 権限: DISTA権限
      loginEnableSelected: [true],     // ログイン: 有効
      loginDisableSelected: [false],     // ログイン: 無効
    },
      {
        validator: Validators.compose([
        ])
      }
    );
  }

  /**
   * 検索処理を行う。
   */
  private search(): Observable<GenericList<MaintUserSearchResult>> {
    // 検索結果をクリア
    this.searchResultItems = [];

    // ユーザを検索
    return this.maintUserService.search(this.generateApiSearchCondition()).pipe(
      tap(genericList => {
        // 次のページのトークンを保存
        this.nextPageToken = genericList.nextPageToken;

        if (genericList.items.length === 0) {
          // 検索結果なし
          this.message.body.error = { code: 'INFO.RESULT_ZERO', param: null };
        } else {
          // 検索結果が1件以上の場合はリストに設定
          this.searchResultItems = genericList.items;
        }
      })
    );
  }

  /**
   * 次の検索処理を行う。
   */
  private nextSearch(): Observable<GenericList<MaintUserSearchResult>> {
    const nextPageToken = this.nextPageToken;
    this.nextPageToken = null;

    // ユーザを検索
    return this.maintUserService.search({ pageToken: nextPageToken } as MaintUserSearchCondition).pipe(
      tap(genericList => {
        // 次のページのトークンを保存
        this.nextPageToken = genericList.nextPageToken;
        this.searchResultItems = this.searchResultItems.concat(genericList.items);
      })
    );
  }

  /**
   * API検索用Modelを設定する。
   * @param maintNewsList ユーザ一覧検索条件
   */
  private generateApiSearchCondition(): MaintUserSearchCondition {
    // ユーザ一覧検索条件
    const searchCondition = new MaintUserSearchCondition();

    // 検索条件設定
    const controls = this.mainForm.controls;

    // キーワード
    const keywords = StringUtils.splitByWhitespace(controls.keyword.value);

    if (keywords.length > 0) {
      // キーワードが入力されている場合
      switch (Number(controls.keywordType.value)) {
        case this.KEYWORD_TYPES.LOGIN_ID:
          // アカウント名（ログインID）
          searchCondition.accountNames = keywords;
          break;
        case this.KEYWORD_TYPES.COMPANY_CODE:
          // 所属会社（会社コード）
          searchCondition.companies = keywords;
          break;
        case this.KEYWORD_TYPES.MAKER:
          // メーカー
          searchCondition.makerCodes = keywords;
          searchCondition.makerNames = keywords;
          break;
        case this.KEYWORD_TYPES.NAME:
          // 名称
          searchCondition.names = keywords;
          break;
        case this.KEYWORD_TYPES.MAIL_ADDRESS:
          // メールアドレス
          searchCondition.mailAddresses = keywords;
          break;
        default:
          // 指定がない場合(「キーワード」が選択)、すべての検索条件にセット
          searchCondition.accountNames = keywords;
          searchCondition.companies = keywords;
          searchCondition.makerCodes = keywords;
          searchCondition.makerNames = keywords;
          searchCondition.names = keywords;
          searchCondition.mailAddresses = keywords;
      }
    }

    // 権限
    const authorities: Authority[] = [];

    if (controls.authorityAdminSelected.value) {
      // 権限: 管理者権限がONの場合
      authorities.push(Authority.ROLE_ADMIN);
    }

    if (controls.authorityJunSelected.value) {
      // 権限: JUN権限がONの場合
      authorities.push(Authority.ROLE_JUN);
    }

    if (controls.authorityMakerSelected.value) {
      // 権限: メーカーがONの場合
      authorities.push(Authority.ROLE_MAKER);
    }

    if (controls.authorityDistaSelected.value) {
      // 権限: ディスタがONの場合
      authorities.push(Authority.ROLE_DISTA);
    }

    if (authorities.length > 0) {
      // 権限が選択されている場合のみ設定（不要なパラメーターは設定しない）
      searchCondition.authorities = authorities;
    }

    // 有効/無効
    const enabledList: boolean[] = [];

    if (controls.loginEnableSelected.value) {
      // ログイン: 有効がONの場合
      enabledList.push(true);
    }

    if (controls.loginDisableSelected.value) {
      // ログイン: 無効がONの場合
      enabledList.push(false);
    }

    if (enabledList.length > 0) {
      // ログイン有効/無効が選択されている場合のみ設定（不要なパラメーターは設定しない）
      searchCondition.enabledList = enabledList;
    }

    // 検索方法（すべてOR検索 部分一致）
    searchCondition.searchMethod = SearchMethod.ALL_OR_LIKE;

    return searchCondition;
  }

  /**
   * スクロール時、最下部までスクロールされた場合は、次の検索結果を取得する。
   * @param event ScrollEvent
   */
  onScroll(event: ScrollEvent): void {
    if (event.isReachingBottom && StringUtils.isNotEmpty(this.nextPageToken)) {
      let loadingToken = null;

      // ローディング開始
      this.loadingService.start().pipe(
        // ローディングトークン待避
        tap((token) => loadingToken = token),
        // メッセージをクリア
        tap(() => this.clearMessage()),
        // ユーザを検索
        flatMap(() => this.nextSearch()),
        // エラーが発生した場合は、エラーモーダルを表示（エラーモーダル表示後は画面非表示）
        catchError((error) => this.messageConfirmModalService.openErrorModal(error)),
        // ローディング停止
        finalize(() => this.loadingService.stop(loadingToken))
      ).subscribe();
    }
  }

  /**
   * 検索ボタン押下時の処理
   */
  onSearch(): void {
    let loadingToken = null;

    // ローディング開始
    this.loadingService.start().pipe(
      // ローディングトークン待避
      tap((token) => loadingToken = token),
      // メッセージをクリア
      tap(() => this.clearMessage()),
      // バリデーションが正常の場合、処理を続行
      filter(() => this.isValidKeyWord()),
      // ユーザを検索
      flatMap(() => this.search()),
      // エラーが発生した場合は、エラーモーダルを表示
      catchError((error) => this.messageConfirmModalService.openErrorModal(error)),
      // ローディング停止
      finalize(() => this.loadingService.stop(loadingToken))
    ).subscribe();
  }

  /**
   * キーワードのバリデーション結果を取得する。バリデーションエラーがある場合、バリデーションエラーメッセージを表示する。
   * @returns バリデーションの結果
   * - true : 正常
   * - false : エラーあり
   */
  private isValidKeyWord(): boolean {
    // キーワード
    const keywords = StringUtils.splitByWhitespace(this.mainForm.controls.keyword.value);

    if (keywords.length > this.CONDITIONS_LIMIT) {
      this.message.body.error = { code: 'INFO.CONDITIONS_LIMIT', param: null };

      return false;
    }

    return true;
  }
}
