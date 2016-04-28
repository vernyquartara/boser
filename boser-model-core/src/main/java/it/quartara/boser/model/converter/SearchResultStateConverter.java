package it.quartara.boser.model.converter;

import static it.quartara.boser.model.SearchResultState.INSERTED;
import static it.quartara.boser.model.SearchResultState.RETRIEVED;

import javax.persistence.AttributeConverter;

import it.quartara.boser.model.SearchResultState;

/**
 * Converts SearchResultsState enum into single character representation.
 * @author webny
 *
 */
public class SearchResultStateConverter implements AttributeConverter<SearchResultState, String> {

	@Override
	public String convertToDatabaseColumn(SearchResultState attribute) {
		switch (attribute) {
		case INSERTED	: return "I";
		case RETRIEVED	: return "R";
		default:
            throw new IllegalArgumentException("Unknown" + attribute);
		}
	}

	@Override
	public SearchResultState convertToEntityAttribute(String dbData) {
		switch (dbData) {
        case "I"	: return INSERTED;
        case "R"	: return RETRIEVED;
        default:
            throw new IllegalArgumentException("Unknown" + dbData);
		}
	}

}
