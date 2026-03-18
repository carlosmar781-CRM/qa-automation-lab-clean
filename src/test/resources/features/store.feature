Feature: Product - Store

  Scenario: Validación del precio de un producto

    Given estoy en la página de la tienda
    When me logueo con mi usuario "test@user.com" y clave "1184258813"
    Then valido que el login fue exitoso
    And navego a la categoria "Clothes" y subcategoria "Men"
    And agrego 2 unidades del primer producto al carrito
    Then valido en el popup la confirmación del producto agregado
    And valido en el popup que el monto total sea calculado correctamente
    When finalizo la compra
    Then valido el titulo de la pagina del carrito
    And vuelvo a validar el calculo de precios en el carrito