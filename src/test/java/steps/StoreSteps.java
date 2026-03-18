package steps;

import io.cucumber.java.en.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.*;

import java.time.Duration;

public class StoreSteps {

    WebDriver driver;
    WebDriverWait wait;

    // Metodo Reutilizable

    private double limpiarPrecio(String texto) {
        texto = texto.replaceAll("[^0-9,.]", "");
        texto = texto.replace(",", ".");
        return Double.parseDouble(texto);
    }

    @Given("estoy en la página de la tienda")
    public void abrirPagina() {

        System.setProperty("webdriver.chrome.driver", "drivers/chromedriver.exe");

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--remote-allow-origins=*");

        driver = new ChromeDriver(options);
        driver.manage().window().maximize();

        wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        driver.manage().deleteAllCookies();

        driver.navigate().to("https://qalab.bensg.com/store/es/");

    }

    @When("me logueo con mi usuario {string} y clave {string}")
    public void login(String user, String pass) {

        WebElement loginBtn = wait.until(
                ExpectedConditions.elementToBeClickable(
                        By.xpath("//a[contains(text(),'Iniciar sesión')]")
                )
        );
        loginBtn.click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("field-email")));

        driver.findElement(By.id("field-email")).sendKeys(user);
        driver.findElement(By.id("field-password")).sendKeys(pass);
        driver.findElement(By.id("submit-login")).click();

        // validar login
        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector(".account")
        ));
    }

    @Then("valido que el login fue exitoso")
    public void validarLogin() {

        WebElement usuario = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.cssSelector(".account")
                )
        );

        if (!usuario.getText().contains("Pepito")) {
            throw new AssertionError("Login no exitoso");
        }
    }

    @When("navego a la categoria {string} y subcategoria {string}")
    public void navegar(String cat, String sub) {

        WebElement categoria = wait.until(
                ExpectedConditions.presenceOfElementLocated(
                        By.xpath("//a[contains(.,'" + cat + "')]")
                )
        );

        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView(true);", categoria
        );

        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].click();", categoria
        );

        WebElement subcategoria = wait.until(
                ExpectedConditions.presenceOfElementLocated(
                        By.xpath("//a[contains(.,'" + sub + "')]")
                )
        );

        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].click();", subcategoria
        );
    }

    @And("agrego 2 unidades del primer producto al carrito")
    public void agregarProducto() {

        wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("article.product-miniature")
        ));

        WebElement producto = wait.until(
                ExpectedConditions.elementToBeClickable(
                        By.cssSelector("article.product-miniature a.product-thumbnail")
                )
        );

        producto.click();

        // Esperar página de producto
        wait.until(ExpectedConditions.presenceOfElementLocated(
                By.id("quantity_wanted")
        ));

        WebElement cantidad = driver.findElement(By.id("quantity_wanted"));

        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].value='2';", cantidad
        );

        cantidad.sendKeys(Keys.TAB);

        // Agregar al carrito
        WebElement botonAgregar = wait.until(
                ExpectedConditions.elementToBeClickable(
                        By.cssSelector("button.add-to-cart")
                )
        );

        botonAgregar.click();
    }

    @Then("valido en el popup la confirmación del producto agregado")
    public void validarPopup() throws InterruptedException {


        Thread.sleep(2000);

        WebElement popup = wait.until(
                ExpectedConditions.presenceOfElementLocated(
                        By.id("blockcart-modal")
                )
        );

        wait.until(driver -> popup.isDisplayed());

        String texto = popup.getText();

        if (!texto.contains("Producto añadido correctamente")) {
            throw new AssertionError("El popup no contiene el mensaje esperado");
        }
    }
    @And("valido en el popup que el monto total sea calculado correctamente")
    public void validarTotalPopup() throws InterruptedException {

        Thread.sleep(2000);

        WebElement popup = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.id("blockcart-modal")
                )
        );

        // 🔥 Precio
        String precioTexto = popup.findElement(
                By.cssSelector(".product-price")
        ).getText();

        double precio = limpiarPrecio(precioTexto);

        // 🔥 Cantidad REAL del popup
        String cantidadTexto = popup.findElement(
                By.xpath(".//*[contains(text(),'Cantidad')]")
        ).getText();

        int cantidad = Integer.parseInt(
                cantidadTexto.replaceAll("[^0-9]", "")
        );

        // 🔥 Total
        String totalTexto = popup.findElement(
                By.xpath(".//*[contains(text(),'Total')]/following-sibling::*")
        ).getText();

        double total = limpiarPrecio(totalTexto);

        double esperado = precio * cantidad;

        System.out.println("Precio: " + precio);
        System.out.println("Cantidad: " + cantidad);
        System.out.println("Esperado: " + esperado);
        System.out.println("Total UI: " + total);

        if (Math.abs(esperado - total) > 0.01) {
            throw new AssertionError("Total incorrecto en popup");
        }
    }
    @When("finalizo la compra")
    public void irCarrito() {

        WebElement botonFinalizar = wait.until(
                ExpectedConditions.elementToBeClickable(
                        By.cssSelector("#blockcart-modal .btn-primary")
                )
        );

        botonFinalizar.click();
    }

    @Then("valido el titulo de la pagina del carrito")
    public void validarTituloCarrito() {

        WebElement titulo = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.cssSelector("h1")
                )
        );

        String texto = titulo.getText().toLowerCase();

        if (!texto.contains("carrito")) {
            throw new AssertionError("No estás en el carrito");
        }
    }

    @And("vuelvo a validar el calculo de precios en el carrito")
    public void validarCarrito() {

        // Precio unitario
        String precioTexto = driver.findElement(
                By.cssSelector(".cart-item .price")
        ).getText();

        double precio = limpiarPrecio(precioTexto);

        // Cantidad
        String cantidadTexto = driver.findElement(
                By.cssSelector(".cart-item .js-cart-line-product-quantity")
        ).getAttribute("value");

        int cantidad = Integer.parseInt(cantidadTexto);

        // Total
        String totalTexto = driver.findElement(
                By.cssSelector(".cart-total .value")
        ).getText();

        double total = limpiarPrecio(totalTexto);

        double esperado = precio * cantidad;

        if (Math.abs(esperado - total) > 0.01) {
            throw new AssertionError("Total incorrecto en carrito");
        }
    }
}