import { HttpClientModule } from '@angular/common/http';
import { Apollo, ApolloModule } from 'apollo-angular';

import { NgModule } from '@angular/core';
import { environment } from '@env/environment';
import { HttpLink } from 'apollo-angular/http';
import { InMemoryCache } from '@apollo/client/core';

@NgModule({
    exports: [
        HttpClientModule,
        ApolloModule,
    ]
})
export class GraphqlModule {
    constructor(
        apollo: Apollo,
        httpLink: HttpLink
    ) {
        // create Apollo
        apollo.create({
            link: httpLink.create({uri: environment.backend + '/graphql'}),
            cache: new InMemoryCache(),
            defaultOptions: {
                query: {
                    fetchPolicy: 'network-only', // disabling cache for fetch
                    errorPolicy: 'ignore',
                },
            },
        });
    }
}
