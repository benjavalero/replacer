import { Component, OnInit } from '@angular/core';
import { Observable } from 'rxjs';
import { AuthenticationService } from 'src/app/authentication/authentication.service';
import { PublicIp } from './public-ip.model';

@Component({
  selector: 'app-public-ip',
  templateUrl: './public-ip.component.html',
  styleUrls: []
})
export class PublicIpComponent implements OnInit {
  publicIp$!: Observable<PublicIp>;

  constructor(private authenticationService: AuthenticationService) {}

  ngOnInit(): void {
    this.publicIp$ = this.authenticationService.getPublicIp$();
  }
}
