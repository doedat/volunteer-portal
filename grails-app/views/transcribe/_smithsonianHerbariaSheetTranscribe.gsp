<%@ page import="au.org.ala.volunteer.FieldCategory; au.org.ala.volunteer.TemplateField; au.org.ala.volunteer.DarwinCoreField" %>
<sitemesh:parameter name="useFluidLayout" value="${true}" />

<div class="container-fluid">

    <div class="row-fluid">
        <div class="span6">
            <div class="well well-small">
                <g:set var="multimedia" value="${taskInstance.multimedia.first()}" />
                <g:imageViewer multimedia="${multimedia}" preserveWidthWhenPinned="true" />
            </div>
        </div>

        <div class="span6">
            <div class="row-fluid" id="taskMetadata">
                <div class="span12">
                    <div class="well well-small">
                        <table style="width: 100%">
                            <tr>
                                <td>
                                    <span class="metaDataLabel">Catalogue No.:</span> ${recordValues?.get(0)?.catalogNumber}
                                    <br/>
                                    <span class="metaDataLabel">Taxa:</span> ${recordValues?.get(0)?.scientificName}
                                </td>
                                <td style="text-align: right">
                                    <span>
                                        <button class="btn" id="show_task_selector" href="#task_selector" style="">Copy from previous task</button>
                                        <a href="#" class="fieldHelp" title="Clicking this button will allow you to select a previously transcribed task to copy values from"><span class="help-container">&nbsp;</span></a>
                                    </span>

                                </td>
                            </tr>
                        </table>

                        <div style="display: none;">
                            <div id="task_selector">
                                <div id="task_selector_content">
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <g:renderFieldCategorySection columns="1" category="${FieldCategory.miscellaneous}" task="${taskInstance}" recordValues="${recordValues}" title="Museum details" description="" />

        </div>
    </div>

    <div class="row-fluid">
        <div class="span6">
            <g:renderFieldCategorySection columns="1" category="${FieldCategory.collectionEvent}" task="${taskInstance}" recordValues="${recordValues}" title="Collection details" />
        </div>
        <div class="span6">
            <g:renderFieldCategorySection columns="1" category="${FieldCategory.location}" task="${taskInstance}" recordValues="${recordValues}" title="Location details" />
        </div>
    </div>

    <g:renderFieldCategorySection category="${FieldCategory.identification}" task="${taskInstance}" recordValues="${recordValues}" title="Identification" description="If a label contains information on the name of the organism then record the name and associated information in this section" />

</div>

<script>

    function setImageHeight(height) {
        $("#image-container").css("height", "" + height + "px");
        $(".imageviewer-controls").css("top", "" + (height - 70) + "px");
        $(".pin-image-control").css("top", "" + (height - 30) + "px");
        $(".show-image-control").css("top", "" + (height - 60) + "px");
    }

    setImageHeight(265);


</script>
