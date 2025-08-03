/**
 * ESLint配置文件
 * 定义项目的代码质量和风格规范
 */
module.exports = {
  root: true,
  env: {
    browser: true,
    es2021: true,
    node: true,
  },
  extends: [
    'plugin:vue/vue3-recommended',
    'eslint:recommended',
    '@typescript-eslint/recommended',
  ],
  parser: 'vue-eslint-parser',
  parserOptions: {
    ecmaVersion: 2021,
    parser: '@typescript-eslint/parser',
    sourceType: 'module',
  },
  plugins: ['vue', '@typescript-eslint'],
  rules: {
    // 基本规则
    'no-console': process.env.NODE_ENV === 'production' ? 'warn' : 'off',
    'no-debugger': process.env.NODE_ENV === 'production' ? 'warn' : 'off',
    'no-unused-vars': 'off', // 使用TypeScript的未使用变量检查
    '@typescript-eslint/no-unused-vars': ['warn', { argsIgnorePattern: '^_' }],
    'no-undef': 'off', // TypeScript已经处理这个问题
    
    // Vue规则
    'vue/multi-word-component-names': ['error', {
      ignores: ['index'] // 只允许index.vue使用单个单词
    }],
    'vue/component-name-in-template-casing': ['error', 'PascalCase'], // 模板中组件名使用PascalCase
    'vue/no-v-html': 'warn', // 提醒XSS风险但不强制禁止
    'vue/require-default-prop': 'error', // 要求props有默认值
    'vue/max-attributes-per-line': ['error', {
      singleline: {
        max: 3
      },
      multiline: {
        max: 1
      }
    }],
    'vue/html-indent': ['error', 2],
    'vue/script-indent': ['error', 2, { baseIndent: 0 }],
    'vue/order-in-components': ['error'], // 组件选项的顺序
    'vue/attributes-order': ['error'], // 标签属性的顺序
    'vue/this-in-template': ['error', 'never'], // 禁止在模板中使用this
    
    // TypeScript规则
    '@typescript-eslint/explicit-module-boundary-types': ['warn'], // 要求导出函数和类的公共类方法的显式返回和参数类型
    '@typescript-eslint/no-explicit-any': 'warn', // 警告使用any类型
    '@typescript-eslint/no-non-null-assertion': 'warn', // 警告非空断言
    '@typescript-eslint/ban-ts-comment': 'warn', // 警告@ts-ignore等注释
    '@typescript-eslint/naming-convention': [
      'error',
      // 变量和函数名使用camelCase
      {
        selector: 'variable',
        format: ['camelCase', 'UPPER_CASE'],
        leadingUnderscore: 'allow'
      },
      // 类名、接口名、类型别名使用PascalCase
      {
        selector: 'typeLike',
        format: ['PascalCase']
      },
      // 枚举成员使用UPPER_CASE
      {
        selector: 'enumMember',
        format: ['UPPER_CASE']
      }
    ],
    
    // 代码风格规则
    'indent': ['error', 2, { SwitchCase: 1 }],
    'quotes': ['error', 'single'],
    'semi': ['error', 'always'],
    'comma-dangle': ['error', 'always-multiline'],
    'arrow-parens': ['error', 'always'],
    'eol-last': ['error', 'always'],
    'object-curly-spacing': ['error', 'always'],
    'max-len': ['warn', { 
      code: 100, 
      ignoreComments: true,
      ignoreUrls: true,
      ignoreStrings: true,
      ignoreTemplateLiterals: true
    }],
    'no-multiple-empty-lines': ['error', { max: 1, maxEOF: 1 }],
    'space-before-function-paren': ['error', {
      anonymous: 'always',
      named: 'never',
      asyncArrow: 'always'
    }],
    'spaced-comment': ['error', 'always', { markers: ['/'] }],
    'brace-style': ['error', '1tbs'],
    'keyword-spacing': ['error', { before: true, after: true }],
    'space-infix-ops': 'error',
    'key-spacing': ['error', { beforeColon: false, afterColon: true }],
    'no-trailing-spaces': 'error',
    'eol-last': 'error',
    'func-call-spacing': ['error', 'never'],
    'no-multi-spaces': 'error',
  },
  overrides: [
    {
      files: ['*.vue'],
      rules: {
        'indent': 'off', // 在Vue文件中关闭indent规则，使用vue/script-indent
      },
    },
    {
      files: ['*.ts', '*.tsx'],
      rules: {
        // TypeScript特定规则
        '@typescript-eslint/explicit-function-return-type': ['warn', {
          allowExpressions: true,
          allowTypedFunctionExpressions: true
        }],
        '@typescript-eslint/member-delimiter-style': ['error', {
          multiline: {
            delimiter: 'semi',
            requireLast: true
          },
          singleline: {
            delimiter: 'semi',
            requireLast: false
          }
        }]
      },
    },
    {
      files: ['**/tests/**/*', '**/__tests__/**/*', '*.spec.ts', '*.test.ts'],
      env: {
        jest: true
      },
      rules: {
        'max-len': 'off',
        '@typescript-eslint/no-explicit-any': 'off'
      }
    }
  ],
};