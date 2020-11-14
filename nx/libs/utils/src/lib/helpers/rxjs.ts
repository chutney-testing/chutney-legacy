import { Observable } from 'rxjs';

export const fromEventSource = (url: string): Observable<MessageEvent> => {
  return new Observable<MessageEvent>((subscriber) => {
    let sse: EventSource;
    try {
      sse = new EventSource(url);
    } catch (e) {
      subscriber.error(e);
    }
    sse.addEventListener('partial', (evt: MessageEvent) => {
      subscriber.next(evt);
    });
    sse.addEventListener('last', (evt: MessageEvent) => {
      subscriber.next(evt);
      subscriber.complete();
    });
    sse.addEventListener('error', (e: MessageEvent) => {
      subscriber.error(e);
    });

    return () => {
      if (sse) {
        sse.close();
      }
    };
  });
};
