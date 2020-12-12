export const chutneySchemaV2 = {
  $schema: 'http://json-schema.org/draft-04/schema#',
  type: 'object',
  definitions: {
    step: {
      type: 'object',
      required: ['description'],
      properties: {
        description: {
          type: 'string',
        },
        subSteps: {
          type: 'array',
          items: {
            $ref: '#/definitions/step',
          }
        },
        implementation: {
          type: 'object',
          properties: {
            type: {
              type: 'string',
              enum: ['selenium-scroll-to',
                'amqp-basic-publish',
                'json-validation',
                'selenium-remote-driver-init',
                'mongo-insert',
                'json-assert',
                'mongo-delete',
                'https-listener',
                'xml-assert',
                'mongo-list',
                'http-put',
                'sleep',
                'selenium-driver-init',
                'http-soap',
                'kafka-basic-publish',
                'http-get',
                'selenium-screen-shot',
                'amqp-basic-get',
                'compare',
                'selenium-click',
                'fail',
                'assert',
                'selenium-set-browser-size',
                'jms-sender',
                'selenium-send-keys',
                'selenium-get',
                'string-assert',
                'selenium-switch-to',
                'kafka-basic-consume',
                'http-delete',
                'selenium-close',
                'success',
                'debug',
                'amqp-unbind-queue',
                'selenium-get-text',
                'jms-clean-queue',
                'context-put',
                'amqp-basic-consume',
                'mongo-update',
                'selenium-get-attribute',
                'mongo-find',
                'jms-listener',
                'amqp-create-bound-temporary-queue',
                'json-compare',
                'mongo-count',
                'http-post',
                'ssh-client',
                'selenium-hover-then-click',
                'amqp-clean-queues',
                'xsd-validation',
                'sql',
                'selenium-wait',
                'https-server-start',
                'https-server-stop',
                'amqp-delete-queue',
                'groovy'],
            },
            target: {
              type: 'string',
              enum: [],
            },
            inputs: {
              type: 'object',
            },
            outputs: {
              type: 'object',
            },
          },
        },
        strategy: {
          type: 'object',
        },
      },
    },
  },
  properties: {
    title: {
      type: 'string',
    },
    description: {
      type: 'string',
    },
    givens: {
      type: 'array',
      items: {
        $ref: '#/definitions/step',
      },
      default: [],
    },
    when: {
      $ref: '#/definitions/step',
    },
    thens: {
      type: 'array',
      items: {
        $ref: '#/definitions/step',
      },
      default: [],
    },
  },
  required: ['title', 'description', 'givens', 'when', 'thens'],
};
