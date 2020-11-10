/**
 * {array} input array which should be filtered against the predicate
 * {predicates} array of predicates which should be run against the given array
 * {shouldPassAll} boolean - optional - true(default) : item should pass all given predicates;
 *                 false - should pass at least one of the predicates
 * {comparator} function - optional : compartor function to sort the resultant array
 */
const filter = (
  array,
  predicates,
  shouldPassAll = true,
  comparator = (a: any, b: any) => 0
) => {
  if (!Array.isArray(array)) {
    throw new Error('Input Array is not valid');
  }

  if (!predicates || !Array.isArray(predicates)) {
    throw new Error('Predicate must be an Array');
  }

  if (shouldPassAll) {
    return array
      .filter((item) => predicates.every((predicate) => predicate(item)))
      .sort(comparator);
  }
  return array
    .filter((item) => predicates.some((predicate) => predicate(item)))
    .sort(comparator);
};

/**
 * Filter array by string
 *
 * @param mainArr
 * @param searchText
 * @returns {any}
 */
const filterArrayByString = (mainArr, searchText): any => {
  if (searchText === '') {
    return mainArr;
  }

  searchText = searchText.toLowerCase();

  return mainArr.filter((itemObj) => {
    return searchInObj(itemObj, searchText);
  });
};

/**
 * Search in object
 *
 * @param itemObj
 * @param searchText
 * @returns {boolean}
 */
const searchInObj = (itemObj, searchText): boolean => {
  for (const prop in itemObj) {
    if (!itemObj.hasOwnProperty(prop)) {
      continue;
    }

    const value = itemObj[prop];

    if (typeof value === 'string') {
      if (searchInString(value, searchText)) {
        return true;
      }
    } else if (Array.isArray(value)) {
      if (searchInArray(value, searchText)) {
        return true;
      }
    }

    if (typeof value === 'object') {
      if (searchInObj(value, searchText)) {
        return true;
      }
    }
  }
};

/**
 * Search in array
 *
 * @param arr
 * @param searchText
 * @returns {boolean}
 */
const searchInArray = (arr, searchText): boolean => {
  for (const value of arr) {
    if (typeof value === 'string') {
      if (searchInString(value, searchText)) {
        return true;
      }
    }

    if (typeof value === 'object') {
      if (searchInObj(value, searchText)) {
        return true;
      }
    }
  }
};

/**
 * Search in string
 *
 * @param value
 * @param searchText
 * @returns {any}
 */
const searchInString = (value, searchText): boolean => {
  return value.toLowerCase().includes(searchText);
};

export { filter, filterArrayByString, searchInObj };
