package com.efficio.fieldbook.web.study.germplasm;

import org.generationcp.commons.parsing.pojo.ImportedGermplasm;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.domain.dms.Enumeration;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.interfaces.GermplasmExportSource;
import org.generationcp.middleware.service.api.OntologyService;
import org.generationcp.middleware.service.api.study.StudyEntryDto;
import org.generationcp.middleware.service.api.study.StudyEntryPropertyData;
import org.generationcp.middleware.service.api.study.StudyGermplasmDto;
import org.springframework.beans.factory.annotation.Configurable;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Configurable
// FIXME: IBP-3697 Is it possible to use ModelMapper instead???
public class StudyGermplasmTransformer {

	public StudyGermplasmTransformer() {
		// this constructor is necessary for aop proxy
	}

	@Resource
	private OntologyService ontologyService;

	@Resource
	private ContextUtil contextUtil;

	public List<ImportedGermplasm> tranformToImportedGermplasm(final List<StudyGermplasmDto> studyGermplasmDtoList) {

		final Map<Integer, String> checkTypesDescriptionMap =
			this.ontologyService.getStandardVariable(TermId.CHECK.getId(), this.contextUtil.getCurrentProgramUUID())
				.getEnumerations().stream().collect(Collectors.toMap(Enumeration::getId, Enumeration::getDescription));

		final List<ImportedGermplasm> importedGermplasmList = new ArrayList<>();
		if (studyGermplasmDtoList != null && !studyGermplasmDtoList.isEmpty()) {
			for (final StudyGermplasmDto studyGermplasmDto : studyGermplasmDtoList) {
				final ImportedGermplasm importedGermplasm = new ImportedGermplasm();
				importedGermplasm.setId(studyGermplasmDto.getEntryId());
				final Integer entryType = studyGermplasmDto.getCheckType();
				if (entryType != null) {
					importedGermplasm.setEntryTypeName(checkTypesDescriptionMap.getOrDefault(entryType, ""));
					importedGermplasm.setEntryTypeValue(entryType.toString());
					importedGermplasm.setEntryTypeCategoricalID(entryType);
				}
				importedGermplasm.setCross(studyGermplasmDto.getCross());
				importedGermplasm.setDesig(studyGermplasmDto.getDesignation());
				importedGermplasm.setEntryCode(studyGermplasmDto.getEntryCode());
				importedGermplasm.setEntryNumber(studyGermplasmDto.getEntryNumber());
				importedGermplasm.setGid(studyGermplasmDto.getGermplasmId().toString());
				importedGermplasm.setMgid(studyGermplasmDto.getGroupId());
				importedGermplasm.setSource(studyGermplasmDto.getSeedSource());
				importedGermplasm.setGroupName(studyGermplasmDto.getCross());
				importedGermplasm.setGroupId(studyGermplasmDto.getGroupId());
				importedGermplasm.setStockIDs(studyGermplasmDto.getStockIds());
				importedGermplasm.setIndex(Integer.valueOf(studyGermplasmDto.getPosition()));
				importedGermplasmList.add(importedGermplasm);
			}
		}
		return importedGermplasmList;
	}

	public List<GermplasmExportSource> tranformToGermplasmExportSource(final List<StudyGermplasmDto> studyGermplasmDtoList) {

		final Map<Integer, String> checkTypesDescriptionMap =
			this.ontologyService.getStandardVariable(TermId.CHECK.getId(), this.contextUtil.getCurrentProgramUUID())
				.getEnumerations().stream().collect(Collectors.toMap(Enumeration::getId, Enumeration::getDescription));

		final List<GermplasmExportSource> germplasmExportSourceList = new ArrayList<>();
		if (studyGermplasmDtoList != null && !studyGermplasmDtoList.isEmpty()) {
			for (final StudyGermplasmDto studyGermplasmDto : studyGermplasmDtoList) {
				final GermplasmExportSource germplasmExportSource = new GermplasmExportSource() {

					@Override
					public Integer getGermplasmId() {
						return studyGermplasmDto.getGermplasmId();
					}

					@Override
					public Integer getCheckType() {
						return studyGermplasmDto.getCheckType();
					}

					@Override
					public String getCheckTypeDescription() {
						return checkTypesDescriptionMap.getOrDefault(studyGermplasmDto.getCheckType(), "");
					}

					@Override
					public Integer getEntryId() {
						return studyGermplasmDto.getEntryNumber();
					}

					@Override
					public String getEntryCode() {
						return studyGermplasmDto.getEntryCode();
					}

					@Override
					public String getSeedSource() {
						return studyGermplasmDto.getSeedSource();
					}

					@Override
					public String getDesignation() {
						return studyGermplasmDto.getDesignation();
					}

					@Override
					public String getGroupName() {
						return studyGermplasmDto.getCross();
					}

					@Override
					public String getFemaleParentDesignation() {
						return null;
					}

					@Override
					public Integer getFemaleGid() {
						return null;
					}

					@Override
					public String getMaleParentDesignation() {
						return null;
					}

					@Override
					public Integer getMaleGid() {
						return null;
					}

					@Override
					public String getStockIDs() {
						return studyGermplasmDto.getStockIds();
					}

					@Override
					public String getSeedAmount() {
						return null;
					}

					@Override
					public Integer getGroupId() {
						return Integer.valueOf(studyGermplasmDto.getGroupId());
					}

					@Override
					public String getNotes() {
						return null;
					}

					@Override
					public Integer getListDataId() {
						return null;
					}
				};

				germplasmExportSourceList.add(germplasmExportSource);
			}
		}

		return germplasmExportSourceList;
	}

	public List<StudyEntryDto> transformToStudyGermplasmDto(final List<ImportedGermplasm> importedGermplasmList) {

		final List<StudyEntryDto> list = new ArrayList<>();
		for (final ImportedGermplasm importedGermplasm : importedGermplasmList) {
			final StudyEntryDto dto = new StudyEntryDto();
			dto.setDesignation(importedGermplasm.getDesig());
			dto.setGid(Integer.valueOf(importedGermplasm.getGid()));
			dto.setEntryCode(importedGermplasm.getEntryCode());
			dto.setEntryNumber(importedGermplasm.getEntryNumber());
			dto.getVariables().put(TermId.ENTRY_TYPE.getId(),
				new StudyEntryPropertyData(null, TermId.ENTRY_TYPE.getId(), String.valueOf(importedGermplasm.getEntryTypeCategoricalID())));
			dto.getVariables().put(TermId.SEED_SOURCE.getId(),
				new StudyEntryPropertyData(null, TermId.SEED_SOURCE.getId(), importedGermplasm.getSource()));
			dto.getVariables().put(TermId.GROUPGID.getId(),
				new StudyEntryPropertyData(null, TermId.GROUPGID.getId(), String.valueOf(importedGermplasm.getGroupId())));
			list.add(dto);
		}
		return list;
	}

}
