<%@ page import="au.org.ala.volunteer.FieldDefinitionType; au.org.ala.volunteer.Project" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="${grailsApplication.config.ala.skin}"/>
        <title><g:message code="admin.label" default="Administration"/></title>
        <style type="text/css">

            .well {
                /*background-color: rgba(245, 245, 245, 0.4);*/
            }

            .dropdown-menu a {
                text-decoration: none;
            }

            .numberCircle {
                border-radius: 50%;
                display: inline-block;
                width: 16px;
                height: 16px;
                padding: 6px;

                background: #fff;
                border: 2px solid #666;
                color: #666;
                text-align: center;

                /*// font: 18px sans-serif;*/
            }


        </style>
        <r:script type='text/javascript'>

            $(document).ready(function () {

                bvp.bindTooltips("a.fieldHelp", 650);

                $(".btnDeleteImage").click(function(e) {
                    var imageName = $(this).attr("imageName");
                    if (imageName) {
                        window.location = "${createLink(controller:'task', action:'unstageImage', params:[projectId: projectInstance.id])}&imageName=" + imageName;
                    }
                });


                $(".btnDeleteShadowFile").click(function(e) {
                    var filename = $(this).attr("filename");
                    if (filename) {
                        window.location = "${createLink(controller:'task', action:'unstageImage', params:[projectId: projectInstance.id])}&imageName=" + filename;
                    }
                });


                $("#btnAddFieldDefinition").click(function(e) {
                    e.preventDefault();
                    var options = {
                        title: "Add field definition",
                        url:"${createLink(action:'editStagingFieldFragment', params:[projectId: projectInstance.id])}"
                    }
                    bvp.showModal(options);
                });

                $(".btnEditField").click(function(e) {
                    e.preventDefault();
                    var fieldId = $(this).parents("[fieldDefinitionId]").attr("fieldDefinitionId");
                    if (fieldId) {
                        var options = {
                            title: "Edit field definition",
                            url:"${createLink(action: 'editStagingFieldFragment', params: [projectId: projectInstance.id])}&fieldDefinitionId=" + fieldId
                        };
                        bvp.showModal(options);
                    }
                });

                $(".btnDeleteField").click(function(e) {
                    var fieldId = $(this).parents("[fieldDefinitionId]").attr("fieldDefinitionId");
                    if (fieldId) {
                        window.location = "${createLink(controller:'task', action:'deleteFieldDefinition', params:[projectId:projectInstance.id])}&fieldDefinitionId=" + fieldId;
                    }
                });

                $("#btnLoadTasks").click(function(e) {
                    e.preventDefault();
                    window.location = "${createLink(controller:'task', action:'loadStagedTasks', params:[projectId: projectInstance.id])}";
                });

                $("#btnExportTasksCSV").click(function(e) {
                    e.preventDefault();
                    window.open("${createLink(controller:"task", action:'exportStagedTasksCSV', params:[projectId: projectInstance.id])}", "ExportCSV");
                });

                $("#btnUploadDataFile").click(function(e) {
                    e.preventDefault();
                    var options = {
                        title: "Upload a data file",
                        url:"${createLink(action:'uploadDataFileFragment', params:[projectId: projectInstance.id])}"
                    }
                    bvp.showModal(options);

                });

                $("#btnClearDataFile").click(function(e) {
                    e.preventDefault();
                    window.location = "${createLink(controller:'task', action:'clearStagedDataFile', params:[projectId: projectInstance.id])}";
                });

                $("#btnClearStagingArea").click(function(e) {
                    e.preventDefault();
                    if (confirm("Are you sure you wish to delete all images from the staging area?")) {
                        window.location = "${createLink(controller:'task', action:'deleteAllStagedImages', params:[projectId: projectInstance.id])}";
                    }
                });

                $("#btnSelectImages").click(function(e) {
                    e.preventDefault();
                    var opts = {
                        title:"Upload images to the staging area",
                        url: "${createLink(action:"selectImagesForStagingFragment", params:[projectId: projectInstance.id])}"
                    };

                    bvp.showModal(opts);
                });

            });

        </r:script>
    </head>

    <body class="sublevel sub-site volunteerportal">

        <cl:headerContent title="Project Task Staging" selectedNavItem="expeditions">
            <%
                pageScope.crumbs = [
                    [link: createLink(controller: 'project', action: 'index', id:projectInstance.id), label: projectInstance.featuredLabel],
                    [link: createLink(controller: 'project', action: 'edit', id:projectInstance.id), label: "Edit"]
                ]
            %>
        </cl:headerContent>

        <div class="row">
            <div class="span12">

                <div class="well well-small">
                    <div class="row-fluid">
                        <div class="span3">
                            <h4><span class="numberCircle">1</span>&nbsp;Upload Images</h4>
                            Upload your images to the staging area
                        </div>
                        <div class="span3">
                            <h4><span class="numberCircle">2</span>&nbsp;Upload datafile (Optional)</h4>
                            <p>
                            Upload a csv file containing extra data to attach to each task. This is useful for prepopulating task data.
                            </p>
                        </div>
                        <div class="span3">
                            <h4><span class="numberCircle">3</span>&nbsp;Configure columns (Optional)</h4>
                            <p>
                            Add and configure columns in the table below to pre-populate data in your tasks.
                                <cl:helpText>
                                    <p>Pre-populated field values can be derived from the image filename, or portions thereof, or can also be read from a separate csv datafile keyed by the image filename.</p>
                                    <p><strong>Note:</strong> Only data displayed in the staged images table will be loaded</p>
                                </cl:helpText>
                            </p>
                            <p>Use the '<i class="icon-cog"></i> Actions' menu to add columns</p>

                        </div>
                        <div class="span3">
                            <h4><span class="numberCircle">4</span>&nbsp;Create tasks</h4>
                            Review the staged images table, and create the tasks.
                        </div>
                    </div>
                </div>

                <div id="uploadImagesSection" class="well well-small">
                    <h4>Upload files to staging area</h4>
                    <p>
                        In addition to task image files, you can also upload auxiliary data files that can contain additional data that should
                        be attached to individual tasks (e.g. OCR text)
                        <cl:helpText markdown="${false}">
                            <div>
                                <p>
                                    Auxiliary files must conform to the following naming scheme in order to be correctly attached:
                                </p>
                                <code>&lt;imageFilename&gt;__&ltdarwinCoreTerm&gt;__&lt;recordIndex&gt;.txt</code>
                                where:
                                <ul>
                                    <li><code>imageFilename</code> matches exactly the name of an image file already uploaded, including the file extension</li>
                                    <li><code>darwinCoreTerm</code> is the name of the field which should be populated with the contents of the file</li>
                                    <li><code>recordIndex</code> is the field index if the same field name can contain multiple values. (defaults to 0 if omitted)</li>
                                </ul>
                                <div class="well well-small">
                                    <p>
                                        For example, assuming an image file has been staged with the name <code>image123.jpg</code>:
                                        <br/>
                                        The contents of <code>image123.jpg__occurrenceRemarks__0.txt</code> will populate the <em>occurrenceRemarks</em> field at index 0
                                    </p>
                                </div>
                            </div>
                        </cl:helpText>
                    </p>
                    <button id="btnSelectImages" class="btn">Select images</button>
                </div>

                <div id="imagesSection" class="">
                    <table style="width:100%; margin-bottom: 5px">
                        <tr>
                            <td><h3>Staged images (${images.size()})</h3></td>
                            <td>
                                %{--<button class="btn btn-success"><i class="icon-upload icon-white"></i>&nbsp;Upload Images</button>--}%

                                <g:if test="${hasDataFile}">
                                    A data file has been uploaded&nbsp;
                                    <button class="btn btn-warning" id="btnClearDataFile">Clear data file</button>
                                    &nbsp;
                                    <a href="${dataFileUrl}">View data file</a>
                                </g:if>
                                <g:else>
                                    No data file has been uploaded&nbsp;
                                    <button class="btn btn-success" id="btnUploadDataFile"><i class="icon-upload icon-white"></i>&nbsp;Upload data file</button>
                                </g:else>

                                <button id="btnLoadTasks" class="btn btn-primary pull-right" style="margin-left: 10px">Create tasks from staged images</button>
                                <div class="btn-group pull-right">
                                    <a class="btn dropdown-toggle" data-toggle="dropdown" href="#">
                                        <i class="icon-cog"></i> Actions
                                        <span class="caret"></span>
                                    </a>
                                    <ul class="dropdown-menu">
                                        <li>
                                            <a href="#" id="btnAddFieldDefinition"><i class="icon-plus"></i>&nbsp;Add a column</a>
                                        </li>
                                        <li class="divider"></li>

                                        <li>
                                            <a href="#" id="btnExportTasksCSV"><i class="icon-file"></i>&nbsp;Export staged tasks as CSV</a>
                                        </li>
                                        <li class="divider"></li>
                                        <li>
                                            <a href="#" id="btnClearStagingArea"><i class="icon-trash"></i>&nbsp;Delete all images</a>
                                        </li>
                                    </ul>
                                </div>

                            </td>
                        </tr>
                    </table>


                    <table class="table table-striped table-bordered">
                        <thead>
                            <tr>
                                <th>
                                    <div>&nbsp;</div>
                                    Image file
                                </th>
                                <g:each in="${profile.fieldDefinitions.sort({it.id})}" var="field">
                                    <th fieldDefinitionId="${field.id}">
                                        <div class="label" style="display: block">
                                            <span style="font-weight: normal">${field.fieldDefinitionType} <b>${field.format}</b></span>

                                            <a href="#" class="btnEditField pull-right" title="Edit column definition"><i class="icon-edit icon-white"></i></a>
                                            <g:if test="${field.fieldName != 'externalIdentifier'}">
                                                <a href="#" class="btnDeleteField pull-right" title="Remove column"><i class="icon-remove icon-white"></i></a>
                                            </g:if>

                                        </div>
                                        ${field.fieldName}<g:if test="${field.recordIndex}">[${field.recordIndex}]</g:if>
                                    </th>
                                </g:each>
                                <th style="width: 40px">
                                    %{--<button id="btnAddFieldDefinition" class="btn btn-small">Add column</button>--}%
                                </th>
                            </tr>
                        </thead>
                        <tbody>
                            <g:each in="${images}" var="image">
                                <tr>
                                    <td>
                                        <a href="${image.url}">${image.name}</a>
                                        <g:if test="${image.shadowFiles}">
                                            <ul class="nav nav-pills nav-stacked" style="margin-left: 10px">
                                                <g:each in="${image.shadowFiles}" var="shadow">
                                                    <li>
                                                        <div class="label">
                                                            <g:set var="shadowLabel" value="${shadow.stagedFile.name.replace(shadow.fieldName, "<em>${shadow.fieldName}</em>")}" />
                                                            <i class="icon-chevron-right icon-white"></i> ${shadowLabel}
                                                            <a href="#" class="btnDeleteShadowFile" title="Delete shadow file ${shadow.stagedFile.name}" filename="${shadow.stagedFile.name}"><i class="icon-remove icon-white"></i></a>
                                                        </div>
                                                    </li>
                                                </g:each>
                                            </ul>
                                        </g:if>
                                    </td>
                                    <g:each in="${profile.fieldDefinitions.sort({it.id})}" var="field">
                                        <td>${image.valueMap[field.fieldName + "_" + field.recordIndex]}</td>
                                    </g:each>
                                    <td>
                                        <button class="btn btn-mini btn-danger btnDeleteImage" imageName="${image.name}"><i class="icon-remove icon-white"></i></button>
                                    </td>
                                </tr>
                            </g:each>
                        </tbody>
                    </table>
                </div>

            </div>
        </div>
    </body>
</html>
