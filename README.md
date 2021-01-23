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
