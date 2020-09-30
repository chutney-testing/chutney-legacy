import { Component } from '@angular/core';

import { StoresAdminService } from '@core/services';
import { GitRepository } from '@model';

@Component({
    selector: 'chutney-stores-admin',
    templateUrl: './stores-admin.component.html',
    styleUrls: ['./stores-admin.component.scss']
})
export class StoresAdminComponent {

    gitRepos: Array<GitRepository> = [];
    errorMessage: String = '';

    constructor(
        private storesAdminService: StoresAdminService
    ) {
        this.load();
    }

    load() {
        this.storesAdminService.findGitRepositories().subscribe(
            (res) => {
                this.gitRepos = res;
            },
            (error) => {
                this.errorMessage = error.message;
            }
        );
    }

    add() {
        this.gitRepos.push(new GitRepository(null, '', '', ''));
    }

    save(repo: GitRepository) {
        this.storesAdminService.saveRepository(repo).subscribe(
            () => {
                this.load();
            },
            (error) => {
                this.errorMessage = error.error;
            }
        );
    }

    delete(repo: GitRepository) {
        this.storesAdminService.deleteRepository(repo).subscribe(
            () => {
                this.load();
            },
            (error) => {
                this.errorMessage = error.error;
            }
        );
    }
}
