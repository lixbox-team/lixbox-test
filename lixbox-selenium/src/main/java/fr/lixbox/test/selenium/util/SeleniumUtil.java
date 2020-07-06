/*******************************************************************************
 *    
 *                           FRAMEWORK Lixbox
 *                          ==================
 *      
 * This file is part of lixbox-test.
 *
 *    lixbox-supervision is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    lixbox-supervision is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *    along with lixbox-test.  If not, see <https://www.gnu.org/licenses/>
 *   
 *   @AUTHOR Lixbox-team
 *
 ******************************************************************************/
package fr.lixbox.test.selenium.util;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Calendar;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import fr.lixbox.common.exceptions.BusinessException;
import fr.lixbox.common.exceptions.ProcessusException;
import fr.lixbox.common.resource.LixboxResources;

/**
 * Cette classe regroupe des facilites pour selenium.
 * 
 * @author ludovic.terral
 */
public class SeleniumUtil
{
    private static final String PATH_DELIMITER = "/";
    // ----------- Attribut -----------
    private static final Log LOG = LogFactory.getLog(SeleniumUtil.class);



    // ----------- Methode -----------
    private SeleniumUtil()
    {
        LOG.trace("Initialisation du SeleniumUtil");
    }
    
    
    
    /**
     * Cette methode verifie la presence d'un element dans le contenu affiche
     *
     * @param driver
     * @param by
     */
    public static boolean isElementPresent(WebDriver driver, By by)
    {
        try
        {
            driver.findElement(by);
            return true;
        }
        catch (NoSuchElementException e)
        {
            LOG.trace(e);
            return false;
        }
    }



    /**
     * Cette methode verifie la presence d'une alerte javascript
     *
     * @param driver
     */
    public static boolean isAlertPresent(WebDriver driver)
    {
        try
        {
            driver.switchTo().alert();
            return true;
        }
        catch (NoAlertPresentException e)
        {
            LOG.trace(e);
            return false;
        }
    }



    /**
     * Cette methode verifie la presence d'une alerte javascript, l'acquitte et
     * renvoir le texte de l'alerte.
     *
     * @param driver
     * @param acceptNextAlert
     * 
     * @return text de l'alerte javascript
     */
    public static String closeAlertAndGetItsText(WebDriver driver, boolean acceptNextAlert)
    {
        Alert alert = driver.switchTo().alert();
        String alertText = alert.getText();
        if (acceptNextAlert)
        {
            alert.accept();
        }
        else
        {
            alert.dismiss();
        }
        return alertText;
    }

        
    
    /**
     * Cette methode indique si un element est present et visible
     * 
     * @param driver
     * @param by
     * @param _timeOut
     */
    public static boolean isElementVisible(WebDriver driver, By by)
    {        
        return SeleniumUtil.isElementPresent(driver, by) && driver.findElement(by).isDisplayed();
    }
    
    
    
    /**
     * Cette methode attend qu'un element soit present
     * 
     * @param driver
     * @param by
     * @param _timeOut
     */
    public static void waitForElementPresent(WebDriver driver, By by, int nTimeOut) throws BusinessException
    {        
        long timeOut = Calendar.getInstance().getTimeInMillis()+nTimeOut;
        while (!(SeleniumUtil.isElementPresent(driver, by) && (driver.findElement(by).isDisplayed())) && (timeOut>Calendar.getInstance().getTimeInMillis()))
        {
            try
            {
                Thread.sleep(1000);
            }
            catch (InterruptedException e)
            {
                LOG.error(e);
            }
        }
        if (!(SeleniumUtil.isElementPresent(driver, by) && driver.findElement(by).isDisplayed()))
        {
            throw new BusinessException(LixboxResources.getString("INFO.PARAM.OBJET.NULL", by.toString()));
        }
    }
    
    
    
    /**
     * Cette methode attend qu'un element soit present et que la valeur est celle attendue
     * 
     * @param driver
     * @param by
     * @param _value
     * @param _timeOut
     * 
     * @throws BusinessException 
     */
    public static void waitForElementValue(WebDriver driver, By by, String sValue, int ntimeOut) throws BusinessException
    {
        long timeOut = Calendar.getInstance().getTimeInMillis()+ntimeOut;
        while (
                !(SeleniumUtil.isElementPresent(driver, by) && (driver.findElement(by).isDisplayed()) && sValue.equals(driver.findElement(by).getAttribute("value"))) && 
                (timeOut>Calendar.getInstance().getTimeInMillis()))
        {
            try
            {
                Thread.sleep(1000);
            }
            catch (InterruptedException e)
            {
                LOG.error(e);
            }
        }
        if (!(SeleniumUtil.isElementPresent(driver, by) && driver.findElement(by).isDisplayed()))
        {
            throw new BusinessException(LixboxResources.getString("INFO.PARAM.OBJET.NULL", by.toString()));
        }
    }

    
    
    /**
     * Cette methode verifie qu'un element est present, il va alors essayer de le rendre visible en scrollant
     * 
     * @param driver
     * @param by
     * 
     * @throws BusinessException 
     */
    public static void tryToRenderElementVisibility(WebDriver driver, By by)
    {
        if (SeleniumUtil.isElementVisible(driver, by))
        {
            JavascriptExecutor jse = (JavascriptExecutor)driver;
            jse.executeScript("arguments[0].scrollIntoView()", driver.findElement(by)); 
            jse.executeScript("window.scrollBy(0,-60)", "");
        }
    }
    
    
    
    /**
     * Cette methode verifie qu'un element est present, il va alors essayer de le rendre visible en scrollant
     * 
     * @param driver
     * @param by
     * 
     * @return WebElement
     * 
     * @throws BusinessException 
     */
    public static WebElement findElement(WebDriver driver, By by)
    {
        WebElement result = driver.findElement(by);
        tryToRenderElementVisibility(driver,by);
        return result;
    }
    
    
    
    
    /**
     * Cette methode effectue un screenshot.
     * 
     * @param driver
     * @param path
     * 
     * @return WebElement
     * 
     * @throws BusinessException 
     */
    public static void takeScreenshot(WebDriver driver, String path)
    {
        try
        {
            byte[] scrFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            FileOutputStream fos = new FileOutputStream(new File(path+PATH_DELIMITER+Calendar.getInstance().getTimeInMillis()+".png"));
            IOUtils.write(scrFile, fos);
            fos.flush();
            fos.close();
        }
        catch (Exception e)
        {
            LOG.fatal(e,e);
            throw new ProcessusException(e);
        }
    }
}