package org.alfresco.po.rm.functional.ManageRules;

import org.alfresco.po.rm.*;
import org.alfresco.po.rm.fileplan.FilePlanPage;
import org.alfresco.po.rm.functional.RmAbstractTest;
import org.alfresco.po.rm.util.RmPageObjectUtils;
import org.alfresco.po.share.ShareUtil;
import org.alfresco.po.share.site.contentrule.createrules.selectors.impl.WhenSelectorImpl;
import org.alfresco.po.share.site.document.FileDirectoryInfo;
import org.alfresco.po.share.util.FailedTestListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.BeforeClass;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import static org.alfresco.po.rm.RmConsoleUsersAndGroups.*;
import static org.alfresco.po.rm.RmFolderRulesWithRules.*;

/**
 * Created by polly on 3/3/14.
 */
@Listeners(FailedTestListener.class)
public class ManageRules extends RmAbstractTest {

    private static Log logger = LogFactory.getLog(ManageRules.class);
    private static final By INPUT_TITLE_SELECTOR = By.name("prop_cm_title");
    private static final By INPUT_DESCRIPTION_SELECTOR = By.name("prop_cm_description");
    private String userName;

    private By descriptionProperty(String description){
        return By.xpath("//span[text()='Description:']/following-sibling::span[text()='" + description + "']");
    }

    /**
     * Executed after class
     */
    @Override
    @AfterClass
    public void doTeardown()
    {
        ShareUtil.logout(drone);
        login();
        deleteRMSite();
    }

    /*
    * RMA-1193:Create rule - no rules defined
    * * Precondition
    * 1. Any user is created
    * 2. Any role with Manage Rules (and View Records) capability is created
    * 3. The user is added to the role
    * 4. Any Category1 is created
    * 5. The user has filling permissions on Category1
    * 6. The user is logged in
    *
    * Click Manage rules for the Category1
    * Verify the available actions
    * Click Create Rules and create any rule
    * Verify that the created rule actually works
     */
    @Test
    public void RMA_1193()
    {
        String testName = Thread.currentThread().getStackTrace()[1].getMethodName().replace("_", "-");
        String userName = testName + RmPageObjectUtils.getRandomString(3);
        String ruleName = testName + RmPageObjectUtils.getRandomString(3);
        String categoryName = testName + RmPageObjectUtils.getRandomString(3);
        String folderName = testName + RmPageObjectUtils.getRandomString(3);

        try
        {
            createRmAdminUser(userName);

            //login as user
            login(drone, userName, DEFAULT_USER_PASSWORD);
            OpenRmSite();
            rmSiteDashBoard.selectFilePlan();
            createCategory(categoryName, true);
            navigateToFolder(categoryName);
            createInboundRule(ruleName, RmActionSelectorEnterpImpl.PerformActions.CLOSE_RECORD_FOLDER);
            rmSiteDashBoard.selectFilePlan();
            FilePlanPage filePlan = navigateToFolder(categoryName);
            createFolder(folderName);
            //Verify that rule applied
            Assert.assertTrue(filePlan.isFolderClosed(drone, folderName), "Failed to apply rule");
        }
        catch (Throwable e)
        {
            reportError(drone, testName, e);
        }
    }

    @Test
    public void RMA_1393()
    {
        String testName = Thread.currentThread().getStackTrace()[1].getMethodName().replace("_", "-");
        String userName = testName + RmPageObjectUtils.getRandomString(3);
        String userName1 = testName + RmPageObjectUtils.getRandomString(3);
        String ruleName1 = testName + RmPageObjectUtils.getRandomString(3);
        String ruleName2 = testName + RmPageObjectUtils.getRandomString(3);
        String folderName = testName + RmPageObjectUtils.getRandomString(3);
        String categoryName = testName + RmPageObjectUtils.getRandomString(3);
        String propertyValue = testName + RmPageObjectUtils.getRandomString(3);

        try
        {
            createRMSite();
            createRmAdminUser(userName);
            createRmAdminUser(userName1);

            //login as user1 and create  rule
            login(drone, userName, DEFAULT_USER_PASSWORD);
            OpenRmSite();
            rmSiteDashBoard.selectFilePlan();
            FilePlanPage filePlan = createCategory(categoryName, true);
            navigateToFolder(categoryName);
            createSetPropertyValueRule(ruleName1, "cm:description", propertyValue, true, true);
            ShareUtil.logout(drone);
            //login as user2 and try to create a rule. Verify that rule applied
            login(drone, userName1, DEFAULT_USER_PASSWORD);
            OpenRmSite();
            rmSiteDashBoard.selectFilePlan();
            navigateToFolder(categoryName);
            createUpdateRule(ruleName2, RmActionSelectorEnterpImpl.PerformActions.CLOSE_RECORD_FOLDER, true, false);
            rmSiteDashBoard.selectFilePlan();
            navigateToFolder(categoryName);
            filePlan = createFolder(folderName);
            //Verify that Inbound rule applied
            filePlan.openDetailsPage(folderName);
            Assert.assertTrue(isElementPresent(descriptionProperty(propertyValue)),
                    "Failed to present Description for Category");

            OpenRmSite();
            navigateToFolder(categoryName);
            //Make some upgrades and verify that Upgrade rule applied
            FileDirectoryInfo folder = filePlan.getFileDirectoryInfo(folderName);
            WebElement selectMoreAction = folder.selectMoreAction();
            selectMoreAction.click();
            WebElement editProperties = folder.findElement(By.cssSelector("div.rm-edit-details>a"));
            editProperties.click();

            drone.waitForElement(INPUT_TITLE_SELECTOR, 5);
            type(INPUT_TITLE_SELECTOR, "updated" + folderName);
            type(INPUT_DESCRIPTION_SELECTOR, "updated" + folderName);

            WebElement saveButton = drone.find(By.cssSelector("button[id$='form-submit-button']"));
            saveButton.click();
            filePlan = (FilePlanPage) rmSiteDashBoard.selectFilePlan();
            filePlan = navigateToFolder(categoryName);
            Assert.assertTrue(filePlan.isFolderClosed(drone, folderName), "Failed to apply rule");

        }
        catch (Throwable e)
        {
            reportError(drone, testName, e);
        }
    }

    /*
    * RMA-1190: Edit rule
    * Precondition
    * 1. Any user is created
    * 2. Any role with Manage Rules  (and View Records) capability is created
    * 3. The user is added to the role
    * 4. Any Category1 is created
    * 5. The user has filling permissions on Category1
    * 5. Any rule is created for the Category1 (e.g. inbound,all items, Add Classifieble aspect)
    * 6. The user is logged in
    *
    * 1 Click Manage Rules for Category1
    * 2 Edit the rule. Change the action or condition (e.g. updated, has tag "tag1", execute script)
    * 3 Verify that the changes to rule are applied. Execute the rule (create the folder, edit it and add a tag "tag1")
    */
    @Test
    public void RMA_1190()
    {
        String testName = Thread.currentThread().getStackTrace()[1].getMethodName().replace("_", "-");
        String userName = testName + RmPageObjectUtils.getRandomString(3);
        String ruleName = testName + RmPageObjectUtils.getRandomString(3);
        String folderName = testName + RmPageObjectUtils.getRandomString(3);
        String categoryName = testName + RmPageObjectUtils.getRandomString(3);
        String propertyValue = testName + RmPageObjectUtils.getRandomString(3);

        try
        {
            createRmAdminUser(userName);
            login(drone, userName, DEFAULT_USER_PASSWORD);
            OpenRmSite();
            rmSiteDashBoard.selectFilePlan();
            FilePlanPage filePlan = createCategory(categoryName, true);
            filePlan = navigateToFolder(categoryName);
            createSetPropertyValueRule(ruleName, "cm:description", propertyValue, false, true);

            OpenRmSite();
            rmSiteDashBoard.selectFilePlan();
            filePlan = navigateToFolder(categoryName);
            //Apply rule
            RmFolderRulesWithRules rulesPage = filePlan.selectManageRulesWithRules().render();
            RmCreateRulePage manageRulesPage = rulesPage.clickEditButton().render();
            WhenSelectorImpl whenSelectorEnter = manageRulesPage.getWhenOptionObj();
            whenSelectorEnter.selectUpdate();
            manageRulesPage.clickSave();
            filePlan = (FilePlanPage) rmSiteDashBoard.selectFilePlan();
            filePlan = navigateToFolder(categoryName);
            createFolder(folderName);
            //Verify that rule applied
            filePlan.openDetailsPage(folderName);
            Assert.assertFalse(isElementPresent(descriptionProperty(propertyValue)),
                    "The Description is present for Folder");

            //Edit Metadata
            OpenRmSite();
            rmSiteDashBoard.selectFilePlan();
            navigateToFolder(categoryName);
            FileDirectoryInfo folder = filePlan.getFileDirectoryInfo(folderName);
            WebElement selectMoreAction = folder.selectMoreAction();
            selectMoreAction.click();
            WebElement editProperties = folder.findElement(By.cssSelector("div.rm-edit-details>a"));
            editProperties.click();

            drone.waitForElement(INPUT_TITLE_SELECTOR, 5);
            type(INPUT_TITLE_SELECTOR, "updated" + folderName);
            type(INPUT_DESCRIPTION_SELECTOR, "updated" + folderName);

            WebElement saveButton = drone.find(By.cssSelector("button[id$='form-submit-button']"));
            saveButton.click();
            filePlan = (FilePlanPage) rmSiteDashBoard.selectFilePlan();
            filePlan = navigateToFolder(categoryName);
            filePlan.openDetailsPage(folderName);
            Assert.assertTrue(isElementPresent(descriptionProperty(propertyValue)),
                    "Failed to present Description for Updated Folder");

        }
        catch (Throwable e)
        {
           reportError(drone, testName, e);
        }
    }

    /*
     * RMA-1192:Delete rule
     * Precondition
     * 1. Any user is created
     * 2. The user is added to RM admins role
     * 3. Any Category1 is created
     * 4. Any rule is created for the Category1 (e.g. inbound, all items, Send email)
     * 5. The user is logged in
     *
     * 1. Click Manage Rules for Category1
     * 2. Delete the rule
     * 3. Verify that the rule is deleted. Try to execute the rule (create the folder)
     */
    @Test
    public void RMA_1192()
    {
        String testName = Thread.currentThread().getStackTrace()[1].getMethodName().replace("_", "-");
        String userName = testName + RmPageObjectUtils.getRandomString(3);
        String ruleName = testName + RmPageObjectUtils.getRandomString(3);
        String categoryName = testName + RmPageObjectUtils.getRandomString(3);
        String folderName = testName + RmPageObjectUtils.getRandomString(3);

        try
        {
            createRmAdminUser(userName);

            //login as user1 and create  rule
            login(drone, userName, DEFAULT_USER_PASSWORD);
            OpenRmSite();
            FilePlanPage filePlan = createCategory(categoryName, true);
            navigateToFolder(categoryName);
            createInboundRule(ruleName, RmActionSelectorEnterpImpl.PerformActions.CLOSE_RECORD_FOLDER);

            filePlan = (FilePlanPage) rmSiteDashBoard.selectFilePlan();
            filePlan = navigateToFolder(categoryName);
            //Delete rule
            RmFolderRulesWithRules rulesPage = filePlan.selectManageRulesWithRules();
            RmFolderRulesPage manageRulesPage = (RmFolderRulesPage) rulesPage.deleteRule(ruleName);

            //Verify that rule does not executed
            filePlan = (FilePlanPage) rmSiteDashBoard.selectFilePlan();
            filePlan = navigateToFolder(categoryName);
            createFolder(folderName);
            Assert.assertFalse(filePlan.isFolderClosed(drone, folderName), "Failed to apply rule");
        }
        catch (Throwable e)
        {
            reportError(drone, testName, e);
        }
    }

    /*
     * RMA-1368: Link to rule set
     * Preconditions
     * 1. Any user is created
     * 2. The user is added to RM admins role
     * 3. The user is logged in
     * 4. Any Category1; Category2 are created
     * 5. Any rule is created for Category2
     * 6. Any Collaboration (public) site is created
     * 7. Any Folder1 is created in the collaboration site
     * 8. Any rule is applied to that Folder1
     *
     * 1. Click Manage rules for the Category1
     * 2. Verify the available actions
     * 3. Click Link to Rule Set and link to rule set created for Folder1
     * 4. Click Link to Rule Set and link to rule set created for Category2
     * 5. Verify that the linked rules actually work for Category1
     */
    //BUG RM-684: https://issues.alfresco.com/jira/browse/RM-684
    @Test
    public void RMA_1368()
    {
        String testName = Thread.currentThread().getStackTrace()[1].getMethodName().replace("_", "-");
        String userName = testName + RmPageObjectUtils.getRandomString(3);
        String ruleName = testName + RmPageObjectUtils.getRandomString(3);
        String ruleName1 = testName + RmPageObjectUtils.getRandomString(3);
        String categoryName = testName + RmPageObjectUtils.getRandomString(3);
        String categoryName1 = testName + RmPageObjectUtils.getRandomString(3);
        String folderName = testName + RmPageObjectUtils.getRandomString(3);
        String folderName1 = testName + RmPageObjectUtils.getRandomString(3);
        String siteName = RmPageObjectUtils.getRandomString(5);

        try
        {
            ShareUtil.logout(drone);
            login();
            deleteRMSite();
            ShareUtil.logout(drone);
            CreateUser(userName);
            login(drone, userName, DEFAULT_USER_PASSWORD);
            createRMSite();
            OpenRmSite();
            //Any Category1; Category2 are created
            createCategory(categoryName, true);
            OpenRmSite();
            rmSiteDashBoard.selectFilePlan();
            createCategory(categoryName1, true);
            //Any rule is created for Category2
            navigateToFolder(categoryName1);
            createInboundRule(ruleName, RmActionSelectorEnterpImpl.PerformActions.CLOSE_RECORD_FOLDER);
            //Any Collaboration (public) site is created

            createCollaborationSite(siteName);
            //Any Folder1 is created in the collaboration site
            createRemoteFolder(siteName, folderName);
            //Any rule is applied to that Folder1
            createRuleForRemoteFolder(folderName, ruleName1);

            OpenRmSite();
            FilePlanPage filePlan = (FilePlanPage) rmSiteDashBoard.selectFilePlan();
            navigateToFolder(categoryName);
            //Click Link to Rule Set and link to rule set created for Folder1
            RmFolderRulesPage manageRulesPage = filePlan.selectManageRules().render();

            manageRulesPage.openLinkToDialog();
            Assert.assertFalse(isElementPresent(By.
                    xpath("//div[contains(@id, 'sitePicker')]//h4[contains(text(), '" + siteName + "')]")),
                    "RM-684: User can link to rule set from Collaboration site");
            //Click Link to Rule Set and link to rule set created for Category2
            OpenRmSite();
            filePlan = (FilePlanPage) rmSiteDashBoard.selectFilePlan();
            navigateToFolder(categoryName);
            linkToRule(categoryName1, ruleName);
            //Verify that rule applied
            OpenRmSite();
            filePlan = (FilePlanPage) rmSiteDashBoard.selectFilePlan();
            filePlan = navigateToFolder(categoryName);
            createFolder(folderName1);
            Assert.assertTrue(filePlan.isFolderClosed(drone, folderName1), "Failed to apply rule");
        }
        catch (Throwable e)
        {
            reportError(drone, testName, e);
        }
        finally {
            ShareUtil.logout(drone);
            login(drone, userName, DEFAULT_USER_PASSWORD);
            deleteRMSite();
        }
    }

    /* RMA-1369:
     * Precondition
     * 1. Any user is created
     * 2. Any role with Manage Rules (and View Records) capability is created
     * 3. The user is added to the role
     * 4. Any Category1 > SubCategory1 > Folder1 > Record1 is created
     * 5. The user has filling permissions on Category1
     * 6. Any rule is created for Category1
     * 7. The user is logged in
     *
     * 1. Click Manage rules for the Category1
     * 2. Verify the available actions
     * 3. Click Run rules for this folder
     */
    @Test
    public void RMA_1369()
    {
        String testName = Thread.currentThread().getStackTrace()[1].getMethodName().replace("_", "-");
        String userName = testName + RmPageObjectUtils.getRandomString(3);
        String ruleName = testName + RmPageObjectUtils.getRandomString(3);
        String categoryName = testName + RmPageObjectUtils.getRandomString(3);
        String subCategoryName = testName + RmPageObjectUtils.getRandomString(3);
        String folderName = testName + RmPageObjectUtils.getRandomString(3);
        String recordName = testName + RmPageObjectUtils.getRandomString(3);
        String propertyValue = testName + RmPageObjectUtils.getRandomString(3);

        try
        {
            ShareUtil.logout(drone);
            login();
            createRMSite();
            createRmAdminUser(userName);

            //login as user
            login(drone, userName, DEFAULT_USER_PASSWORD);
            OpenRmSite();
            rmSiteDashBoard.selectFilePlan();
            //Create category
            createCategory(categoryName, true);
            navigateToFolder(categoryName);
            //Create SubCategory
            createCategory(subCategoryName, false);
            webDriverWait(drone, 2000);
            navigateToFolder(subCategoryName);
            //Create Folder
            createFolder(folderName);
            navigateToFolder(folderName);
            //Create Record
            webDriverWait(drone, 2000);
            createRecord(recordName);

            //Any rule is created for Category1
            rmSiteDashBoard.selectFilePlan();
            navigateToFolder(categoryName);
            createSetPropertyValueRule(ruleName, "cm:description", propertyValue, false, true);
            //Click Manage rules for the Category1
            //Verify the available actions
            Assert.assertTrue(isElementPresent(EDIT_BUTTON), "Failed To Present Edit Rule Button");
            Assert.assertTrue(isElementPresent(DELETE_BUTTON), "Failed To Present Delete Rule Button");
            Assert.assertTrue(isElementPresent(NEW_RULE_BUTTON), "Failed To Present New Rule Button");
            Assert.assertTrue(isElementPresent(RUN_RULES_BUTTON), "Failed To Present Run Rules Button");
            click(RUN_RULES_BUTTON);
            Assert.assertTrue(isElementPresent(RUN_RULES_FOR_FOLDER),
                    "Failed To Present Run Rules For Folder Button");
            Assert.assertTrue(isElementPresent(RUN_RULES_FOR_SUBFOLDER),
                    "Failed To Present Run Rules For FolderAnd Subfolders Button");
            click(RUN_RULES_FOR_FOLDER);
            Assert.assertFalse(isFailureMessageAppears(), "Failed to run rule for Category");
//            waitUntilCreatedAlert();
            webDriverWait(drone, 2000);
            OpenRmSite();
            FilePlanPage fileplan = rmSiteDashBoard.selectFilePlan().render();
            //Verify that Rule applied for Category
            fileplan.openDetailsPage(categoryName);
            Assert.assertFalse(isElementPresent(descriptionProperty(propertyValue)),
                    "The Description is present for Category");
            //Verify that Rule applied for SubCategory
            OpenRmSite();
            fileplan = rmSiteDashBoard.selectFilePlan().render();
            navigateToFolder(categoryName);
            fileplan.openDetailsPage(subCategoryName);
            Assert.assertTrue(isElementPresent(descriptionProperty(propertyValue)),
                    "Failed to present Description for SubCategory");
            rmSiteDashBoard.selectFilePlan().render();
            navigateToFolder(categoryName);
            fileplan = navigateToFolder(subCategoryName);
            fileplan.openDetailsPage(folderName);
            Assert.assertFalse(isElementPresent(descriptionProperty(propertyValue)),
                    "The Description is present for Folder");
            rmSiteDashBoard.selectFilePlan().render();
            navigateToFolder(categoryName);
            navigateToFolder(subCategoryName);
            fileplan = navigateToFolder(folderName);
            fileplan.openRecordDetailsPage(recordName);
            Assert.assertFalse(isElementPresent(descriptionProperty(propertyValue)),
                    "The Description is present for Record");
        }
        catch (Throwable e)
        {
            reportError(drone, testName, e);
        }
    }

    @Test
    public void RMA_1370()
    {
        String testName = Thread.currentThread().getStackTrace()[1].getMethodName().replace("_", "-");
        String userName = testName + RmPageObjectUtils.getRandomString(3);
        String ruleName = testName + RmPageObjectUtils.getRandomString(3);
        String categoryName = testName + RmPageObjectUtils.getRandomString(3);
        String subCategoryName = testName + RmPageObjectUtils.getRandomString(3);
        String folderName = testName + RmPageObjectUtils.getRandomString(3);
        String recordName = testName + RmPageObjectUtils.getRandomString(3);
        String propertyValue = testName + RmPageObjectUtils.getRandomString(3);

        try
        {
            createRmAdminUser(userName);

            //login as user
            login(drone, userName, DEFAULT_USER_PASSWORD);
            OpenRmSite();
            rmSiteDashBoard.selectFilePlan();
            //Create category
            createCategory(categoryName, true);
            navigateToFolder(categoryName);
            //Create SubCategory
            createCategory(subCategoryName, false);
            webDriverWait(drone, 2000);
            navigateToFolder(subCategoryName);
            //Create Folder
            createFolder(folderName);
            navigateToFolder(folderName);
            //Create Record
            webDriverWait(drone, 2000);
            createRecord(recordName);

            //Any rule is created for Category1
            rmSiteDashBoard.selectFilePlan();
            navigateToFolder(categoryName);
            navigateToFolder(subCategoryName);
            createSetPropertyValueRule(ruleName, "cm:description", propertyValue, false, true);
            //Click Manage rules for the Category1
            //Verify the available actions
            Assert.assertTrue(isElementPresent(EDIT_BUTTON), "Failed To Present Edit Rule Button");
            Assert.assertTrue(isElementPresent(DELETE_BUTTON), "Failed To Present Delete Rule Button");
            Assert.assertTrue(isElementPresent(NEW_RULE_BUTTON), "Failed To Present New Rule Button");
            Assert.assertTrue(isElementPresent(RUN_RULES_BUTTON), "Failed To Present Run Rules Button");
            click(RUN_RULES_BUTTON);
            Assert.assertTrue(isElementPresent(RUN_RULES_FOR_FOLDER),
                    "Failed To Present Run Rules For Folder Button");
            Assert.assertTrue(isElementPresent(RUN_RULES_FOR_SUBFOLDER),
                    "Failed To Present Run Rules For FolderAnd Subfolders Button");
            click(RUN_RULES_FOR_FOLDER);
            Assert.assertFalse(isFailureMessageAppears(), "Failed to run rule for SubCategory");
//            waitUntilCreatedAlert();
            webDriverWait(drone, 2000);
            OpenRmSite();
            FilePlanPage fileplan = rmSiteDashBoard.selectFilePlan().render();
            //Verify that Rule applied for Category
            fileplan.openDetailsPage(categoryName);
            Assert.assertFalse(isElementPresent(descriptionProperty(propertyValue)),
                    "The Description is present for Category");
            //Verify that Rule applied for SubCategory
            OpenRmSite();
            fileplan = rmSiteDashBoard.selectFilePlan().render();
            navigateToFolder(categoryName);
            fileplan.openDetailsPage(subCategoryName);
            Assert.assertTrue(isElementPresent(descriptionProperty(propertyValue)),
                    "Failed to present Description for SubCategory");
            rmSiteDashBoard.selectFilePlan().render();
            navigateToFolder(categoryName);
            fileplan = navigateToFolder(subCategoryName);
            fileplan.openDetailsPage(folderName);
            Assert.assertTrue(isElementPresent(descriptionProperty(propertyValue)),
                    "Failed to present Description for Folder");
            rmSiteDashBoard.selectFilePlan().render();
            navigateToFolder(categoryName);
            navigateToFolder(subCategoryName);
            fileplan = navigateToFolder(folderName);
            fileplan.openRecordDetailsPage(recordName);
            Assert.assertFalse(isElementPresent(descriptionProperty(propertyValue)),
                    "The Description is present for Record");
        }
        catch (Throwable e)
        {
            reportError(drone, testName, e);
        }
    }

    @Test
    public void RMA_1371()
    {
        String testName = Thread.currentThread().getStackTrace()[1].getMethodName().replace("_", "-");
        String userName = testName + RmPageObjectUtils.getRandomString(3);
        String ruleName = testName + RmPageObjectUtils.getRandomString(3);
        String categoryName = testName + RmPageObjectUtils.getRandomString(3);
        String subCategoryName = testName + RmPageObjectUtils.getRandomString(3);
        String folderName = testName + RmPageObjectUtils.getRandomString(3);
        String recordName = testName + RmPageObjectUtils.getRandomString(3);
        String propertyValue = testName + RmPageObjectUtils.getRandomString(3);

        try
        {
            createRmAdminUser(userName);

            //login as user
            login(drone, userName, DEFAULT_USER_PASSWORD);
            OpenRmSite();
            rmSiteDashBoard.selectFilePlan();
            //Create category
            createCategory(categoryName, true);
            navigateToFolder(categoryName);
            //Create SubCategory
            createCategory(subCategoryName, false);
            webDriverWait(drone, 2000);
            navigateToFolder(subCategoryName);
            //Create Folder
            createFolder(folderName);
            navigateToFolder(folderName);
            //Create Record
            webDriverWait(drone, 2000);
            createRecord(recordName);

            //Any rule is created for Category1
            rmSiteDashBoard.selectFilePlan();
            navigateToFolder(categoryName);
            navigateToFolder(subCategoryName);
            navigateToFolder(folderName);
            createSetPropertyValueRule(ruleName, "cm:description", propertyValue, false, true);
            //Click Manage rules for the Category1
            //Verify the available actions
            Assert.assertTrue(isElementPresent(EDIT_BUTTON), "Failed To Present Edit Rule Button");
            Assert.assertTrue(isElementPresent(DELETE_BUTTON), "Failed To Present Delete Rule Button");
            Assert.assertTrue(isElementPresent(NEW_RULE_BUTTON), "Failed To Present New Rule Button");
            Assert.assertTrue(isElementPresent(RUN_RULES_BUTTON), "Failed To Present Run Rules Button");
            click(RUN_RULES_BUTTON);
            Assert.assertTrue(isElementPresent(RUN_RULES_FOR_FOLDER),
                    "Failed To Present Run Rules For Folder Button");
            Assert.assertTrue(isElementPresent(RUN_RULES_FOR_SUBFOLDER),
                    "Failed To Present Run Rules For FolderAnd Subfolders Button");
            click(RUN_RULES_FOR_FOLDER);
            Assert.assertFalse(isFailureMessageAppears(), "Failed to run rule for Folder");
            webDriverWait(drone, 2000);
            OpenRmSite();
            FilePlanPage fileplan = rmSiteDashBoard.selectFilePlan().render();
            //Verify that Rule applied for Category
            fileplan.openDetailsPage(categoryName);
            Assert.assertFalse(isElementPresent(descriptionProperty(propertyValue)),
                    "The Description is present for Category");
            //Verify that Rule applied for SubCategory
            OpenRmSite();
            fileplan = rmSiteDashBoard.selectFilePlan().render();
            navigateToFolder(categoryName);
            fileplan.openDetailsPage(subCategoryName);
            Assert.assertFalse(isElementPresent(descriptionProperty(propertyValue)),
                    "The Description is present for SubCategory");
            rmSiteDashBoard.selectFilePlan().render();
            navigateToFolder(categoryName);
            fileplan = navigateToFolder(subCategoryName);
            fileplan.openDetailsPage(folderName);
            Assert.assertFalse(isElementPresent(descriptionProperty(propertyValue)),
                    "The Description is present for Folder");
            rmSiteDashBoard.selectFilePlan().render();
            navigateToFolder(categoryName);
            navigateToFolder(subCategoryName);
            fileplan = navigateToFolder(folderName);
            fileplan.openRecordDetailsPage(recordName);
            Assert.assertTrue(isElementPresent(descriptionProperty(propertyValue)),
                    "Failed to present Description for Record");
        }
        catch (Throwable e)
        {
            reportError(drone, testName, e);
        }
    }

    //BUG RM-1273 https://issues.alfresco.com/jira/browse/RM-1273
    @Test
    public void RMA_1372()
    {
        String testName = Thread.currentThread().getStackTrace()[1].getMethodName().replace("_", "-");
        String userName = testName + RmPageObjectUtils.getRandomString(3);
        String ruleName = testName + RmPageObjectUtils.getRandomString(3);
        String categoryName = testName + RmPageObjectUtils.getRandomString(3);
        String subCategoryName = testName + RmPageObjectUtils.getRandomString(3);
        String folderName = testName + RmPageObjectUtils.getRandomString(3);
        String recordName = testName + RmPageObjectUtils.getRandomString(3);
        String propertyValue = testName + RmPageObjectUtils.getRandomString(3);

        try
        {
            createRmAdminUser(userName);

            //login as user
            login(drone, userName, DEFAULT_USER_PASSWORD);
            OpenRmSite();
            rmSiteDashBoard.selectFilePlan();
            //Create category
            createCategory(categoryName, true);
            navigateToFolder(categoryName);
            //Create SubCategory
            createCategory(subCategoryName, false);
            webDriverWait(drone, 2000);
            navigateToFolder(subCategoryName);
            //Create Folder
            createFolder(folderName);
            navigateToFolder(folderName);
            //Create Record
            webDriverWait(drone, 2000);
            createRecord(recordName);

            //Any rule is created for Category1
            rmSiteDashBoard.selectFilePlan();
            createSetPropertyValueRule(ruleName, "cm:description", propertyValue, true, true);
            //Click Manage rules for the Category1
            //Verify the available actions
            Assert.assertTrue(isElementPresent(EDIT_BUTTON), "Failed To Present Edit Rule Button");
            Assert.assertTrue(isElementPresent(DELETE_BUTTON), "Failed To Present Delete Rule Button");
            Assert.assertTrue(isElementPresent(NEW_RULE_BUTTON), "Failed To Present New Rule Button");
            Assert.assertTrue(isElementPresent(RUN_RULES_BUTTON), "Failed To Present Run Rules Button");
            click(RUN_RULES_BUTTON);
            Assert.assertTrue(isElementPresent(RUN_RULES_FOR_FOLDER),
                    "Failed To Present Run Rules For Folder Button");
            Assert.assertTrue(isElementPresent(RUN_RULES_FOR_SUBFOLDER),
                    "Failed To Present Run Rules For Folder And Subfolders Button");
            click(RUN_RULES_FOR_SUBFOLDER);
            Assert.assertFalse(isFailureMessageAppears(),
                    "RM-1273: The rule created for root-node of the File plan are applied to Holds, " +
                            "Transfers and Unfiled Records");
//            waitUntilCreatedAlert();
            webDriverWait(drone, 2000);
            OpenRmSite();
            FilePlanPage fileplan = rmSiteDashBoard.selectFilePlan().render();
            //Verify that Rule applied for Category
            fileplan.openDetailsPage(categoryName);
            Assert.assertTrue(isElementPresent(descriptionProperty(propertyValue)),
                    "Failed to present Description for Category");
            //Verify that Rule applied for SubCategory
            OpenRmSite();
            fileplan = rmSiteDashBoard.selectFilePlan().render();
            navigateToFolder(categoryName);
            fileplan.openDetailsPage(subCategoryName);
            Assert.assertTrue(isElementPresent(descriptionProperty(propertyValue)),
                    "Failed to present Description for SubCategory");
            rmSiteDashBoard.selectFilePlan().render();
            navigateToFolder(categoryName);
            fileplan = navigateToFolder(subCategoryName);
            fileplan.openDetailsPage(folderName);
            Assert.assertTrue(isElementPresent(descriptionProperty(propertyValue)),
                    "Failed to present Description for Folder");
            rmSiteDashBoard.selectFilePlan().render();
            navigateToFolder(categoryName);
            navigateToFolder(subCategoryName);
            fileplan = navigateToFolder(folderName);
            fileplan.openRecordDetailsPage(recordName);
            Assert.assertTrue(isElementPresent(descriptionProperty(propertyValue)),
                    "Failed to present Description for Record");
        }
        catch (Throwable e)
        {
            reportError(drone, testName, e);
        }
        finally {
            ShareUtil.logout(drone);
            login();
            deleteRMSite();
        }
    }
}