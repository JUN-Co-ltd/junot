import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { NewsTagType } from '../../const/const';
import { ExceptionUtils } from '../../util/exception-utils';

import { News } from '../../model/news';

import { HeaderService } from '../../service/header.service';
import { NewsService } from '../../service/news.service';

@Component({
  selector: 'app-news-detail',
  templateUrl: './news-detail.component.html',
  styleUrls: ['./news-detail.component.scss']
})
export class NewsDetailComponent implements OnInit {
  NEWS_TAG_TYPE = NewsTagType;

  news: News = null;

  constructor(
    private route: ActivatedRoute,
    private headerService: HeaderService,
    private newsService: NewsService
  ) { }

  ngOnInit() {
    this.headerService.show();
    const id = this.route.snapshot.params['id'];
    this.newsService.get(id).subscribe(news => {
      this.news = news;
    }, error => {
      let message = 'ERRORS.ANY_ERROR';
      const apiError = ExceptionUtils.apiErrorHandler(error);
      if (apiError != null) {
        message = apiError.viewErrorMessageCode;
      }
      ExceptionUtils.displayErrorInfo('getDataErrorInfo', message);
    });
  }
}
