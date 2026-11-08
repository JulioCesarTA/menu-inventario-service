# menu-inventario-service

Recetas, platos, menú semanal, insumos por lote, stock, mermas y órdenes de compra.

| | |
|---|---|
| Stack | Java 21 · Spring Boot |
| Arquitectura de código | DDD (Domain-Driven Design) |
| Base de datos | MongoDB |
| Nube de despliegue | AWS (EKS) |
| Puerto local | `8081` (dentro del contenedor, `8080`) |

Publica eventos de dominio (`StockBajo`, `PorcionApartada`, ...) al bus SNS/SQS. Apartar y liberar porciones es parte de la saga de reservas.

## Probar

```bash
./mvnw test
docker build -t menu-inventario-service .
```

## Ejecutar con todo el sistema

Este repositorio es solo un microservicio. Para levantar el sistema completo (bases de datos, Keycloak, gateways, frontends) usa
[`comedor-infra`](https://github.com/JulioCesarTA/comedor-infra) con los demás repositorios clonados en la misma carpeta:

```bash
git clone https://github.com/JulioCesarTA/comedor-infra && cd comedor-infra && bash clonar-todo.sh
docker compose up -d --build
```

## CI/CD

- `.github/workflows/ci.yml`: pruebas y construcción de la imagen en cada push y pull request.
- `.github/workflows/cd.yml`: construye y sube la imagen al registro de AWS y aplica el manifiesto de
  `comedor-infra/infra/k8s/aws/menu-inventario-service/`. Secretos necesarios: ver el propio archivo.
