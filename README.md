## 1ª edição Bootcamp Matera - Construindo APIs REST com Spring

```

```

* ## Estrutura das aulas

  * **Aula 1**
      * Apresentação do curso e explicação de conceitos importantes sobre APIs, protocolo HTTP, REST, documentação e segurança
      * [Materiais Google Classroom - aula 1](https://classroom.google.com/c/MjIzNDc2OTExMTMx/m/MjU3NDI1NjA0NzYy/details)
      * [Branch - aula 1](https://github.com/materasystems/bootcamp-1-spring/tree/aula1)
  * **Aula 2**
      * Apresentação de conceitos e estrutura de módulos do Spring Framework
      * Criação do projeto inicial com [Spring Initializr](https://start.spring.io/)
      * [Materiais Google Classroom - aula 2](https://classroom.google.com/c/MjIzNDc2OTExMTMx/m/MjU4MDE5NzQ4NzE2/details)
      * [Branch - aula 2](https://github.com/materasystems/bootcamp-1-spring/tree/aula2)
  * **Aula 3**
      * Entendendo o contexto do Spring
        * Inversão de controle
        * Injeção de dependências
      * Criação de beans no contexto do Spring e possíveis problemas
        * Uso da interface **ApplicationRunner**
        * Uso de *annotations* **@Component**, **@Autowired**, **@Primary** e **@Qualifier**
      * JPA
      * Spring Data
      * H2 console
      * Criação da estrutura do pacote **entity** com as entidades do banco de dados
      * Conexão estabelecida com banco de dados
      * Criação da estrutura do pacote **repository**
      * Usando a classe **AppStartupRunner** para alimentar a base de dados do H2 utilizando as classes do pacote **repository**
      * [Branch - aula 3](https://github.com/materasystems/bootcamp-1-spring/tree/aula3)
  * **Aula 4**
      * Finalizando mapeamento de entidades e repositórios
      * Refatorando entidades com a criação da classe **EntidadeBase**
      * Refatorando entidades com o uso de Lombok
      * [Branch - aula 4](https://github.com/materasystems/bootcamp-1-spring/tree/aula4)
  * **Aula 5**
      * Criação da estrutura do pacote **dto** com classes de *Request* e *Response*
      * Criação da estrutura do pacote **service**
      * Criando consultas necessárias nas classes *Repository* para as validações feitas nas classes *Service*
      * [Branch - aula 5](https://github.com/materasystems/bootcamp-1-spring/tree/aula5)
  * **Aula 6**
      * Correção de alguns problemas na classe **ClienteService**
      * Uso da *annotation* **@Value** para buscar a configuração agencia.numeroMaximo no arquivo de propriedades da aplicação
      * Alterações nos enums **Natureza**, **SituacaoConta** e **TipoLancamento** e seus mapeamentos para não gravar o ordinal ou a string dos enums
      * Refatorando *exceptions* para uso da classe **ServiceException**
      * Finalizando consultas necessárias nas classes *Repository* para as validações feitas nas classes *Service*
      * Finalizando criação das classes de *Request* e *Response* no pacote **dto**
      * Finalizando criação das regras de negócio nas classes do pacote **service**
      * [Branch - aula 6](https://github.com/materasystems/bootcamp-1-spring/tree/aula6)
  * **Aula 7**
      * Finalizando métodos da classe **ContaService**
      * Criação da estrutura do pacote **controller** com requisições *GET*, *POST* e *PUT* para a entidade de Cliente
      * Tratamento de códigos de retorno HTTP
      * Requisições via Postman
      * Anotações do Jackson para formatação do JSON
      * *Exception handler* para tratamento de exceções
      * Criação de requisições *POST* para a entidade de Conta
      * [Branch - aula 7](https://github.com/materasystems/bootcamp-1-spring/tree/aula7)
  * **Aula 8**
      * Finalizando requisições *POST*, *GET* e *DELETE* para a entidade de Conta
      * Usando a classe **AppStartupRunner** para alimentar a base de dados do H2 utilizando as classes do pacote **service**
      * Importando *collection* pronta com os *requests* das APIs no Postman
      * Validações nos *requests* usando anotações e tratando exceções de DTOs anotados com **@Valid** usando *Exception handler*
      * Efetuando *debug* de uma requisição REST pelo Eclipse
      * [Branch - aula 8](https://github.com/materasystems/bootcamp-1-spring/tree/aula8)
