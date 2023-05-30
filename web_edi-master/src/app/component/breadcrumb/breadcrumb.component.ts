import { Component, OnInit } from '@angular/core';
import { Router, ActivatedRoute, NavigationEnd, Params, PRIMARY_OUTLET } from '@angular/router';
import { filter } from 'rxjs/operators';

import { BreadcrumbLabel } from '../../const/const';

// パンくずインタフェース
interface IBreadcrumb {
  label: string;
  params: Params;
  url: string;
}

@Component({
  selector: 'app-breadcrumb',
  templateUrl: './breadcrumb.component.html',
  styleUrls: ['./breadcrumb.component.scss']
})
export class BreadcrumbComponent implements OnInit {

  // パンくず型定義
  public breadcrumbs: IBreadcrumb[];

  // 最初のディレクトリ処理中か
  private isFirstDirectory = true;

  constructor(
    private activatedRoute: ActivatedRoute,
    private router: Router
  ) {
    this.breadcrumbs = [];
  }

  /**
   * 初期処理表示
   * getBreadcrumbsからパンくず要素を取得し、表示する。
   */
  ngOnInit() {
    this.router.events.pipe(
      filter(event => event instanceof NavigationEnd)
    ).subscribe(event => {
      const root: ActivatedRoute = this.activatedRoute.root;
      this.isFirstDirectory = true;
      this.breadcrumbs = this.getBreadcrumbs(root);
    });
  }

  /**
   * パンくず作成処理
   * app-routing.module.tsのroutesからパンくず要素を取得する。
   * トップの配下ではないURLでもパンくずの先頭にトップを設定する。
   * @param route
   * @param url
   * @param breadcrumbs
   * @returns IBreadcrumb[]
   */
  private getBreadcrumbs(route: ActivatedRoute, url: string = '', breadcrumbs: IBreadcrumb[] = []): IBreadcrumb[] {
    const ROUTE_DATA_BREADCRUMB = 'breadcrumb';

    // children要素を取得する。
    const children: ActivatedRoute[] = route.children;

    // childrenが0件ならそのまま返却
    if (children.length === 0) {
      return breadcrumbs;
    }

    // childrenが1件以上なら設定して返却
    for (const child of children) {
      const VISIBLE = child.snapshot.data['visible'];
      console.debug('breadCrumbsVisible:', VISIBLE);
      if (VISIBLE === false) {
        breadcrumbs = [];
        return;
      }

      if (child.outlet !== PRIMARY_OUTLET) {
        continue;
      }

      // パンくずの設定がないディレクトリであれば次の処理へ
      if (!child.snapshot.data.hasOwnProperty(ROUTE_DATA_BREADCRUMB)) {
        return this.getBreadcrumbs(child, url, breadcrumbs);
      }

      const routeURL: string = child.snapshot.url.map(segment => segment.path).join('/');

      // ディレクトリ名がなければ次の処理へ
      if (routeURL === '') {
        return this.getBreadcrumbs(child, url, breadcrumbs);
      }

      const params = child.snapshot.params;
      console.debug('params:', params);

      const label = child.snapshot.data[ROUTE_DATA_BREADCRUMB];

      // 先頭のディレクトリがトップではない場合、パンくずにトップを設定する
      if (this.isFirstDirectory && label !== BreadcrumbLabel.TOP) {
        breadcrumbs = this.setTopBreadcrumb(breadcrumbs, params);
      }

      url += `/${ routeURL }`;
      console.debug('url:', url);

      const breadcrumb: IBreadcrumb = {
        label: label,
        params: params,
        url: url
      };
      breadcrumbs.push(breadcrumb);

      this.isFirstDirectory = false;
      return this.getBreadcrumbs(child, url, breadcrumbs);
    }
  }

  /**
   * パンくずのトップを設定する。
   * @param breadcrumbs
   * @param params
   * @return IBreadcrumb[]
   */
  private setTopBreadcrumb(breadcrumbs: IBreadcrumb[], params: Params): IBreadcrumb[] {
    const breadcrumb: IBreadcrumb = {
      label: BreadcrumbLabel.TOP,
      params: params,
      url: '/top'
    };
    breadcrumbs.push(breadcrumb);
    return breadcrumbs;
  }
}
